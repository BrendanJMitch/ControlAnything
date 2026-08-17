package com.brendan.controlanything.data.mqtt

sealed interface MqttConnectionState {
    data object Disconnected : MqttConnectionState
    data object Connecting : MqttConnectionState
    data object Connected : MqttConnectionState
    data class Error(val message: String) : MqttConnectionState
}
