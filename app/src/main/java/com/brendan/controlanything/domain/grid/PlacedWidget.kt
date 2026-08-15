package com.brendan.controlanything.domain.grid

/**
 * [key] is the stable identity of the underlying control/output (its topic, or a
 * joystick's topicX) - not tied to any UI/Compose concept.
 */
data class PlacedWidget(
    val key: String,
    val position: GridPosition,
)
