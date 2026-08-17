package com.brendan.controlanything.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brendan.controlanything.data.discovery.NsdDiscoveryRepository
import com.brendan.controlanything.data.discovery.WifiBindingHelper
import com.brendan.controlanything.data.mqtt.MqttConnectionState
import com.brendan.controlanything.data.mqtt.MqttRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val wifiBindingHelper: WifiBindingHelper,
    private val discoveryRepository: NsdDiscoveryRepository,
    private val mqttRepository: MqttRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Searching)
    val uiState = _uiState.asStateFlow()

    private val navigateToDashboardChannel = Channel<Unit>(Channel.CONFLATED)
    val navigateToDashboard: Flow<Unit> = navigateToDashboardChannel.receiveAsFlow()

    private var connectAttempted = false
    private var discoveryStarted = false

    init {
        viewModelScope.launch {
            mqttRepository.connectionState.collect { state ->
                if (state is MqttConnectionState.Error) {
                    _uiState.value = DiscoveryUiState.Error(state.message)
                }
            }
        }
        viewModelScope.launch {
            mqttRepository.deviceInfo.filterNotNull().collect {
                navigateToDashboardChannel.trySend(Unit)
            }
        }
    }

    /** Must not be called until ACCESS_LOCAL_NETWORK has been granted - Wi-Fi binding and NSD both need it. */
    fun startDiscovery() {
        if (discoveryStarted) return
        discoveryStarted = true
        viewModelScope.launch {
            wifiBindingHelper.bindToWifiNetwork().collect {
                startDiscoveryIfNeeded()
            }
        }
    }

    private fun startDiscoveryIfNeeded() {
        if (connectAttempted) return
        viewModelScope.launch {
            discoveryRepository.discoverBrokers().collect { broker ->
                if (connectAttempted) return@collect
                connectAttempted = true
                _uiState.value = DiscoveryUiState.Connecting(broker.serviceName)
                mqttRepository.connect(broker.host, broker.port)
            }
        }
    }
}
