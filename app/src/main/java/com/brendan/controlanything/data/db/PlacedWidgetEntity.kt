package com.brendan.controlanything.data.db

import androidx.room.Entity

/**
 * One row per placed widget within a saved dashboard layout, keyed by projectId alone (not the
 * schema hash) - a topic's position is reused across firmware schema changes as long as the
 * topic itself still exists; if it doesn't, the row is simply never looked up again.
 */
@Entity(tableName = "placed_widgets", primaryKeys = ["projectId", "topicKey"])
data class PlacedWidgetEntity(
    val projectId: String,
    val topicKey: String,
    val col: Int,
    val row: Int,
    val colSpan: Int,
    val rowSpan: Int,
)
