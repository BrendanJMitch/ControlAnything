package com.brendan.controlanything.data.mqtt

import kotlinx.serialization.Serializable

/**
 * DTOs mirroring the wire shape of the retained "info" topic exactly. Kept separate from the
 * domain model (ControlDef/OutputDef/DeviceInfo) so raw-string widget type/topic fields never
 * leak past the mapping boundary.
 */
@Serializable
data class InfoMessage(
    val device_id: String,
    val device_name: String,
    val project_id: String,
    val schema_hash: String,
    val controls: List<WidgetSpecJson> = emptyList(),
    val outputs: List<WidgetSpecJson> = emptyList(),
)

/**
 * One control or output entry. Every widget type shares this same top-level shape - [topic] is
 * always a list (single-element for most widgets), and any widget-specific configuration (a
 * slider's min/max, a joystick using topic[0]/topic[1] as its x/y axes, etc.) lives in [widget].
 */
@Serializable
data class WidgetSpecJson(
    val topic: List<String>,
    val display_name: String,
    val type: String,
    val widget: WidgetJson,
)

@Serializable
data class WidgetJson(
    val type: String,
    val min: Double? = null,
    val max: Double? = null,
)
