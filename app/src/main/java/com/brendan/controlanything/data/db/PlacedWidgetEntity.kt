package com.brendan.controlanything.data.db

import androidx.room.Entity

/** One row per placed widget within a saved (projectId, schemaHash) dashboard layout. */
@Entity(tableName = "placed_widgets", primaryKeys = ["projectId", "schemaHash", "topicKey"])
data class PlacedWidgetEntity(
    val projectId: String,
    val schemaHash: String,
    val topicKey: String,
    val col: Int,
    val row: Int,
    val colSpan: Int,
    val rowSpan: Int,
)
