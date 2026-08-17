package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.OutputDef

/** Unrecognized widget_type values are dropped (with a log) rather than failing the whole parse. */
fun InfoMessage.toDeviceInfo(): DeviceInfo = DeviceInfo(
    deviceId = device_id,
    deviceName = device_name,
    projectId = project_id,
    schemaHash = schema_hash,
    controls = controls.mapNotNull { it.toControlDef() },
    outputs = outputs.mapNotNull { it.toOutputDef() },
)

private fun ControlJson.toControlDef(): ControlDef? = when (widget_type) {
    "toggle" -> topic?.let { ControlDef.Toggle(it, display_name) }
    "button" -> topic?.let { ControlDef.Button(it, display_name) }
    "slider" -> topic?.let {
        ControlDef.Slider(it, display_name, (min ?: 0.0).toFloat(), (max ?: 1.0).toFloat())
    }
    "joystick" -> if (topic_x != null && topic_y != null) {
        ControlDef.Joystick(topic_x, topic_y, display_name)
    } else {
        null
    }
    else -> null
}

private fun OutputJson.toOutputDef(): OutputDef? = when (widget_type) {
    "numeric_readout" -> OutputDef.NumericReadout(topic, display_name)
    "led_indicator" -> OutputDef.LedIndicator(topic, display_name)
    else -> null
}
