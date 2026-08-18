package com.brendan.controlanything.ui.dashboard

import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DeviceInfo
import com.brendan.controlanything.domain.model.MqttValue

data class DashboardUiState(
    val deviceInfo: DeviceInfo? = null,
    val columnCount: Int = DEFAULT_COLUMN_COUNT,
    val positions: List<PlacedWidget> = emptyList(),
    val outputValues: Map<String, MqttValue> = emptyMap(),
    val controlValues: Map<String, MqttValue> = emptyMap(),
)
