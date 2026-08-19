package com.brendan.controlanything.data.layout

import com.brendan.controlanything.data.db.DashboardLayoutDao
import com.brendan.controlanything.data.db.DashboardSettingsEntity
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DashboardOrientation
import javax.inject.Inject

class RoomLayoutRepository @Inject constructor(
    private val dao: DashboardLayoutDao,
) : LayoutRepository {

    override suspend fun loadLayout(projectId: String, schemaHash: String): SavedLayout? {
        val settings = dao.getSettings(projectId, schemaHash) ?: return null
        val widgets = dao.getWidgets(projectId, schemaHash)
        if (widgets.isEmpty()) return null
        return SavedLayout(
            positions = widgets.map { it.toDomain() },
            columnCount = settings.columnCount,
            orientation = settings.orientation.toDashboardOrientation(),
        )
    }

    override suspend fun saveWidgetPosition(projectId: String, schemaHash: String, widget: PlacedWidget) {
        dao.upsertWidget(widget.toEntity(projectId, schemaHash))
    }

    override suspend fun saveWidgetPositions(projectId: String, schemaHash: String, widgets: List<PlacedWidget>) {
        dao.upsertWidgets(widgets.map { it.toEntity(projectId, schemaHash) })
    }

    override suspend fun saveSettings(projectId: String, schemaHash: String, columnCount: Int, orientation: DashboardOrientation) {
        dao.upsertSettings(DashboardSettingsEntity(projectId, schemaHash, columnCount, orientation.name))
    }
}
