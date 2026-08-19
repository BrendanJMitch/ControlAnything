package com.brendan.controlanything.data.mqtt

import com.brendan.controlanything.domain.model.ButtonMode
import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.LedColor
import com.brendan.controlanything.domain.model.OutputDef
import com.brendan.controlanything.domain.model.SliderOrientation
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonPrimitive

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
    "toggle" -> topic.getOrNull(0)?.let {
        ControlDef.Toggle(
            topic = it,
            displayName = display_name,
            defaultValue = widget.default_value?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }
    "button" -> topic.getOrNull(0)?.let {
        ControlDef.Button(
            topic = it,
            displayName = display_name,
            mode = widget.mode.toButtonMode(),
        )
    }
    "slider" -> topic.getOrNull(0)?.let {
        val min = (widget.min ?: 0.0).toFloat()
        val max = (widget.max ?: 1.0).toFloat()
        val defaultValue = (widget.default_value?.jsonPrimitive?.floatOrNull ?: min).coerceIn(min, max)
        ControlDef.Slider(
            topic = it,
            displayName = display_name,
            min = min,
            max = max,
            defaultValue = defaultValue,
            orientation = widget.orientation.toSliderOrientation(),
        )
    }
    "joystick" -> {
        val topicX = topic.getOrNull(0)
        val topicY = topic.getOrNull(1)
        if (topicX != null && topicY != null) ControlDef.Joystick(topicX, topicY, display_name) else null
    }
    else -> null
}

private fun WidgetSpecJson.toOutputDef(): OutputDef? = when (widget.type) {
    "numeric_readout" -> topic.getOrNull(0)?.let {
        OutputDef.NumericReadout(topic = it, displayName = display_name, suffix = widget.suffix ?: "")
    }
    "led_indicator" -> topic.getOrNull(0)?.let {
        OutputDef.LedIndicator(topic = it, displayName = display_name, color = widget.color.toLedColor())
    }
    else -> null
}

private fun String?.toButtonMode(): ButtonMode =
    ButtonMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ButtonMode.STATE

private fun String?.toSliderOrientation(): SliderOrientation =
    SliderOrientation.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: SliderOrientation.HORIZONTAL

private fun String?.toLedColor(): LedColor =
    LedColor.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: LedColor.GREEN
