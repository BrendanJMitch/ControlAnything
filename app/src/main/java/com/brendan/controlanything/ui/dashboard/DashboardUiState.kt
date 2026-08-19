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
    val orientation: DashboardOrientation = DashboardOrientation.PORTRAIT,
)

/**
 * Auto-rotate doesn't work with a grid laid out for one screen dimension, so the dashboard locks
 * the Activity to whichever orientation is chosen here instead. Saved per dashboard alongside
 * column count once Room persistence lands (M6).
 */
enum class DashboardOrientation { PORTRAIT, LANDSCAPE }
