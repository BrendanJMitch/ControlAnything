package com.brendan.controlanything.domain.model

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val projectId: String,
    val schemaHash: String,
    val controls: List<ControlDef>,
    val outputs: List<OutputDef>,
)
