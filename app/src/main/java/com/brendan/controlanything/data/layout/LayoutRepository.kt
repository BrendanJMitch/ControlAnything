package com.brendan.controlanything.data.layout

import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DashboardOrientation

data class SavedLayout(
    val positions: List<PlacedWidget>,
    val columnCount: Int,
    val orientation: DashboardOrientation,
)

interface LayoutRepository {
    /**
     * Whatever's been saved so far for this project, if anything - positions may be a sub/superset
     * of the project's current widget set, since a topic's saved position is kept even after the
     * topic itself disappears from a later firmware schema (it simply goes unused).
     */
    suspend fun loadLayout(projectId: String): SavedLayout?

    suspend fun saveWidgetPosition(projectId: String, widget: PlacedWidget)

    suspend fun saveWidgetPositions(projectId: String, widgets: List<PlacedWidget>)

    suspend fun saveSettings(projectId: String, columnCount: Int, orientation: DashboardOrientation)
}
