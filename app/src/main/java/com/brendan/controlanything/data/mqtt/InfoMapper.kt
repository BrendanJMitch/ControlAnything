package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.OutputDef

/** Unrecognized widget types (or a topic list too short for what the widget needs) are dropped rather than failing the whole parse. */
fun InfoMessage.toDeviceInfo(): DeviceInfo = DeviceInfo(
    deviceId = device_id,
    deviceName = device_name,
    projectId = project_id,
    schemaHash = schema_hash,
    controls = controls.mapNotNull { it.toControlDef() },
    outputs = outputs.mapNotNull { it.toOutputDef() },
)

private fun WidgetSpecJson.toControlDef(): ControlDef? = when (widget.type) {
    "toggle" -> topic.getOrNull(0)?.let { ControlDef.Toggle(it, display_name) }
    "button" -> topic.getOrNull(0)?.let { ControlDef.Button(it, display_name) }
    "slider" -> topic.getOrNull(0)?.let {
        ControlDef.Slider(it, display_name, (widget.min ?: 0.0).toFloat(), (widget.max ?: 1.0).toFloat())
    }
    "joystick" -> {
        val topicX = topic.getOrNull(0)
        val topicY = topic.getOrNull(1)
        if (topicX != null && topicY != null) ControlDef.Joystick(topicX, topicY, display_name) else null
    }
    else -> null
}

private fun WidgetSpecJson.toOutputDef(): OutputDef? = when (widget.type) {
    "numeric_readout" -> topic.getOrNull(0)?.let { OutputDef.NumericReadout(it, display_name) }
    "led_indicator" -> topic.getOrNull(0)?.let { OutputDef.LedIndicator(it, display_name) }
    else -> null
}
