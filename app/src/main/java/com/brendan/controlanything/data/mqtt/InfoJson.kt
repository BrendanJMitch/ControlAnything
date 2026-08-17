package com.brendan.controlanything.data.mqtt

import kotlinx.serialization.Serializable

/**
 * DTOs mirroring the wire shape of the retained "info" topic exactly. Kept separate from the
 * domain model (ControlDef/OutputDef/DeviceInfo) so raw-string widget_type/type fields never
 * leak past the mapping boundary.
 */
@Serializable
data class InfoMessage(
    val device_id: String,
    val device_name: String,
    val project_id: String,
    val schema_hash: String,
    val controls: List<ControlJson> = emptyList(),
    val outputs: List<OutputJson> = emptyList(),
)

@Serializable
data class ControlJson(
    val topic: String? = null,
    val topic_x: String? = null,
    val topic_y: String? = null,
    val display_name: String,
    val type: String,
    val widget_type: String,
    val min: Double? = null,
    val max: Double? = null,
)

@Serializable
data class OutputJson(
    val topic: String,
    val display_name: String,
    val type: String,
    val widget_type: String,
)
