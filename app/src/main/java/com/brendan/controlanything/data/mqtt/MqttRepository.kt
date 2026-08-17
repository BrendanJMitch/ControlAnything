package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MqttRepository {
    val connectionState: StateFlow<MqttConnectionState>

    /** Parsed contents of the retained "info" topic, once received. */
    val deviceInfo: StateFlow<DeviceInfo?>

    fun connect(host: String, port: Int)

    fun disconnect()

    /** [topic] is a leaf name; the repository prepends "outputs/". */
    fun observeTopic(topic: String): Flow<String>

    /** [topic] is a leaf name; the repository prepends "controls/". */
    fun publish(topic: String, payload: String, retained: Boolean = false)
}
