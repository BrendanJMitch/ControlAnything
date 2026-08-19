package com.brendan.controlanything.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brendan.controlanything.data.layout.LayoutRepository
import com.brendan.controlanything.data.mqtt.MqttRepository
import com.brendan.controlanything.domain.grid.GridEngine
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.DashboardOrientation
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.MqttValue
import com.brendan.controlanything.domain.model.OutputDef
import com.brendan.controlanything.domain.model.SliderOrientation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal const val DEFAULT_COLUMN_COUNT = 4

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mqttRepository: MqttRepository,
    private val layoutRepository: LayoutRepository,
) : ViewModel() {

    private val columnCount = MutableStateFlow(DEFAULT_COLUMN_COUNT)
    private val positions = MutableStateFlow<List<PlacedWidget>>(emptyList())
    private val outputValues = MutableStateFlow<Map<String, MqttValue>>(emptyMap())
    private val controlValues = MutableStateFlow<Map<String, MqttValue>>(emptyMap())
    private val orientation = MutableStateFlow(DashboardOrientation.PORTRAIT)

    // kotlinx.coroutines' typed combine() only goes up to 5 flows - nest to stay type-safe at 6.
    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            mqttRepository.deviceInfo,
            columnCount,
            positions,
            outputValues,
            ::CoreState,
        ),
        controlValues,
        orientation,
    ) { core, controlValues, orientation ->
        DashboardUiState(core.deviceInfo, core.columnCount, core.positions, core.outputValues, controlValues, orientation)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private var observeOutputsJob: Job? = null
    private var lastSchemaHash: String? = null
    private var currentProjectId: String? = null

    init {
        viewModelScope.launch {
            mqttRepository.deviceInfo.filterNotNull().collect { deviceInfo ->
                if (deviceInfo.schemaHash != lastSchemaHash) {
                    lastSchemaHash = deviceInfo.schemaHash
                    currentProjectId = deviceInfo.projectId
                    loadOrInitializeLayout(deviceInfo)
                    observeOutputs(deviceInfo)
                    seedControlDefaults(deviceInfo)
                }
            }
        }
    }

    /** Reuses a saved layout for this exact (projectId, schemaHash) if one exists, otherwise auto-places everything and saves that as the starting point. */
    private fun loadOrInitializeLayout(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            val saved = layoutRepository.loadLayout(deviceInfo.projectId, deviceInfo.schemaHash)
            if (saved != null) {
                positions.value = saved.positions
                columnCount.value = saved.columnCount
                orientation.value = saved.orientation
            } else {
                placeWidgets(deviceInfo)
                layoutRepository.saveWidgetPositions(deviceInfo.projectId, deviceInfo.schemaHash, positions.value)
                layoutRepository.saveSettings(deviceInfo.projectId, deviceInfo.schemaHash, columnCount.value, orientation.value)
            }
        }
    }

    private fun placeWidgets(deviceInfo: DeviceInfo) {
        val placed = mutableListOf<PlacedWidget>()
        val entries = deviceInfo.controls.map { it.topic to it.defaultSpan() } +
            deviceInfo.outputs.map { it.topic to it.defaultSpan() }
        entries.forEach { (key, span) ->
            val (colSpan, rowSpan) = span
            val position = GridEngine.nextFreeCell(placed, columnCount.value, colSpan, rowSpan)
            placed += PlacedWidget(key, position)
        }
        positions.value = placed
    }

    private fun observeOutputs(deviceInfo: DeviceInfo) {
        observeOutputsJob?.cancel()
        outputValues.value = emptyMap()
        observeOutputsJob = viewModelScope.launch {
            deviceInfo.outputs.forEach { output ->
                launch {
                    mqttRepository.observeTopic(output.topic).collect { raw ->
                        val value = when (output) {
                            is OutputDef.NumericReadout -> raw.toFloatOrNull()?.let { MqttValue.Number(it) }
                            is OutputDef.LedIndicator -> raw.toBooleanStrictOrNull()?.let { MqttValue.Bool(it) }
                        }
                        if (value != null) {
                            outputValues.value = outputValues.value + (output.topic to value)
                        }
                    }
                }
            }
        }
    }

    /** [newPosition] is assumed already validated (WidgetFrame checks collisions before calling this). */
    fun onWidgetMoved(key: String, newPosition: GridPosition) {
        positions.value = positions.value.map { if (it.key == key) it.copy(position = newPosition) else it }
        val schemaHash = lastSchemaHash ?: return
        val projectId = currentProjectId ?: return
        val widget = positions.value.firstOrNull { it.key == key } ?: return
        viewModelScope.launch { layoutRepository.saveWidgetPosition(projectId, schemaHash, widget) }
    }

    fun onColumnCountChanged(newColumnCount: Int) {
        positions.value = GridEngine.rescale(positions.value, columnCount.value, newColumnCount)
        columnCount.value = newColumnCount
        persistPositionsAndSettings()
    }

    fun onOrientationChanged(newOrientation: DashboardOrientation) {
        orientation.value = newOrientation
        persistSettings()
    }

    private fun persistSettings() {
        val schemaHash = lastSchemaHash ?: return
        val projectId = currentProjectId ?: return
        viewModelScope.launch { layoutRepository.saveSettings(projectId, schemaHash, columnCount.value, orientation.value) }
    }

    private fun persistPositionsAndSettings() {
        val schemaHash = lastSchemaHash ?: return
        val projectId = currentProjectId ?: return
        viewModelScope.launch {
            layoutRepository.saveWidgetPositions(projectId, schemaHash, positions.value)
            layoutRepository.saveSettings(projectId, schemaHash, columnCount.value, orientation.value)
        }
    }

    /** Seeds a neutral starting value for every control - the app is the source of truth for what it last commanded. */
    private fun seedControlDefaults(deviceInfo: DeviceInfo) {
        val defaults = mutableMapOf<String, MqttValue>()
        deviceInfo.controls.forEach { control ->
            when (control) {
                is ControlDef.Toggle -> defaults[control.topic] = MqttValue.Bool(control.defaultValue)
                is ControlDef.Slider -> defaults[control.topic] = MqttValue.Number(control.defaultValue)
                is ControlDef.Joystick -> {
                    defaults[control.topicX] = MqttValue.Number(0f)
                    defaults[control.topicY] = MqttValue.Number(0f)
                }
                is ControlDef.Button -> Unit
            }
        }
        controlValues.value = defaults
    }

    /** [topic] is a leaf name (e.g. a joystick axis); publishes are never retained for controls. */
    fun onControlChanged(topic: String, value: MqttValue) {
        controlValues.value = controlValues.value + (topic to value)
        val payload = when (value) {
            is MqttValue.Bool -> value.value.toString()
            is MqttValue.Number -> value.value.toString()
        }
        mqttRepository.publish(topic, payload, retained = false)
    }
}

/** (colSpan, rowSpan) a freshly-placed widget starts at, before the user resizes it manually. */
private fun ControlDef.defaultSpan(): Pair<Int, Int> = when (this) {
    is ControlDef.Toggle -> 1 to 1
    is ControlDef.Button -> 2 to 1
    is ControlDef.Slider -> when (orientation) {
        SliderOrientation.HORIZONTAL -> 2 to 1
        SliderOrientation.VERTICAL -> 1 to 2
    }
    is ControlDef.Joystick -> 2 to 2
}

private fun OutputDef.defaultSpan(): Pair<Int, Int> = when (this) {
    is OutputDef.NumericReadout -> 2 to 1
    is OutputDef.LedIndicator -> 1 to 1
}

/** Intermediate holder so the 6-flow combine above can stay type-safe via nesting. */
private data class CoreState(
    val deviceInfo: DeviceInfo?,
    val columnCount: Int,
    val positions: List<PlacedWidget>,
    val outputValues: Map<String, MqttValue>,
)
