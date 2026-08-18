package com.brendan.controlanything.domain.model

sealed class MqttValue {
    data class Bool(val value: Boolean) : MqttValue()
    data class Number(val value: Float) : MqttValue()
}
