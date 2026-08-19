package com.brendan.controlanything.data.layout

import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DashboardOrientation

data class SavedLayout(
    val positions: List<PlacedWidget>,
    val columnCount: Int,
    val orientation: DashboardOrientation,
)

interface LayoutRepository {
    /** Null if nothing has been saved yet for this (projectId, schemaHash) pair. */
    suspend fun loadLayout(projectId: String, schemaHash: String): SavedLayout?

    suspend fun saveWidgetPosition(projectId: String, schemaHash: String, widget: PlacedWidget)

    suspend fun saveWidgetPositions(projectId: String, schemaHash: String, widgets: List<PlacedWidget>)

    suspend fun saveSettings(projectId: String, schemaHash: String, columnCount: Int, orientation: DashboardOrientation)
}
