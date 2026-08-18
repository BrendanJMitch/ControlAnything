package com.brendan.controlanything.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brendan.controlanything.data.mqtt.MqttRepository
import com.brendan.controlanything.domain.grid.GridEngine
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.MqttValue
import com.brendan.controlanything.domain.model.OutputDef
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
private const val DEFAULT_COL_SPAN = 2
private const val DEFAULT_ROW_SPAN = 1

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mqttRepository: MqttRepository,
) : ViewModel() {

    private val columnCount = MutableStateFlow(DEFAULT_COLUMN_COUNT)
    private val positions = MutableStateFlow<List<PlacedWidget>>(emptyList())
    private val outputValues = MutableStateFlow<Map<String, MqttValue>>(emptyMap())

    val uiState: StateFlow<DashboardUiState> = combine(
        mqttRepository.deviceInfo,
        columnCount,
        positions,
        outputValues,
    ) { deviceInfo, columnCount, positions, outputValues ->
        DashboardUiState(deviceInfo, columnCount, positions, outputValues)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private var observeOutputsJob: Job? = null
    private var lastSchemaHash: String? = null

    init {
        viewModelScope.launch {
            mqttRepository.deviceInfo.filterNotNull().collect { deviceInfo ->
                if (deviceInfo.schemaHash != lastSchemaHash) {
                    lastSchemaHash = deviceInfo.schemaHash
                    placeWidgets(deviceInfo)
                    observeOutputs(deviceInfo)
                }
            }
        }
    }

    private fun placeWidgets(deviceInfo: DeviceInfo) {
        val placed = mutableListOf<PlacedWidget>()
        val keys = deviceInfo.controls.map { it.topic } + deviceInfo.outputs.map { it.topic }
        keys.forEach { key ->
            val position = GridEngine.nextFreeCell(placed, columnCount.value, DEFAULT_COL_SPAN, DEFAULT_ROW_SPAN)
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
    }

    fun onColumnCountChanged(newColumnCount: Int) {
        positions.value = GridEngine.rescale(positions.value, columnCount.value, newColumnCount)
        columnCount.value = newColumnCount
    }
}
