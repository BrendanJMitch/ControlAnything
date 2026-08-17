package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.DeviceInfo
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Singleton
class HiveMqRepository @Inject constructor() : MqttRepository {

    private var client: Mqtt3AsyncClient? = null

    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    override val deviceInfo = _deviceInfo.asStateFlow()

    // Every subscribed topic funnels through here rather than one Paho/HiveMQ callback per widget.
    private val incomingMessages = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 64)

    override fun connect(host: String, port: Int) {
        _connectionState.value = MqttConnectionState.Connecting
        _deviceInfo.value = null

        val mqttClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("controlanything-${UUID.randomUUID()}")
            .serverHost(host)
            .serverPort(port)
            .addConnectedListener {
                _connectionState.value = MqttConnectionState.Connected
            }
            .addDisconnectedListener { context ->
                _connectionState.value = MqttConnectionState.Error(
                    context.cause?.message ?: "Disconnected from broker"
                )
            }
            .buildAsync()
        client = mqttClient

        mqttClient.connectWith().send().whenComplete { _, throwable ->
            if (throwable != null) {
                _connectionState.value = MqttConnectionState.Error(throwable.message ?: "Connection failed")
                return@whenComplete
            }
            subscribeAll(mqttClient)
        }
    }

    private fun subscribeAll(mqttClient: Mqtt3AsyncClient) {
        mqttClient.subscribeWith()
            .topicFilter(INFO_TOPIC)
            .callback { publish -> handleInfoMessage(publish) }
            .send()

        mqttClient.subscribeWith()
            .topicFilter("$OUTPUTS_PREFIX#")
            .callback { publish -> handleOutputMessage(publish) }
            .send()
    }

    private fun handleInfoMessage(publish: Mqtt3Publish) {
        val payload = publish.payloadAsBytes.toString(Charsets.UTF_8)
        runCatching { Json.decodeFromString<InfoMessage>(payload).toDeviceInfo() }
            .onSuccess { _deviceInfo.value = it }
    }

    private fun handleOutputMessage(publish: Mqtt3Publish) {
        val topic = publish.topic.toString().removePrefix(OUTPUTS_PREFIX)
        val payload = publish.payloadAsBytes.toString(Charsets.UTF_8)
        incomingMessages.tryEmit(topic to payload)
    }

    override fun observeTopic(topic: String): Flow<String> =
        incomingMessages.filter { it.first == topic }.map { it.second }

    override fun publish(topic: String, payload: String, retained: Boolean) {
        client?.publishWith()
            ?.topic("$CONTROLS_PREFIX$topic")
            ?.payload(payload.toByteArray())
            ?.retain(retained)
            ?.send()
    }

    override fun disconnect() {
        client?.disconnect()
        client = null
        _connectionState.value = MqttConnectionState.Disconnected
        _deviceInfo.value = null
    }

    private companion object {
        const val INFO_TOPIC = "info"
        const val CONTROLS_PREFIX = "controls/"
        const val OUTPUTS_PREFIX = "outputs/"
    }
}
