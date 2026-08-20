package com.brendan.controlanything.data.layout

import com.brendan.controlanything.data.db.PlacedWidgetEntity
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DashboardOrientation

/** Pulled out from RoomLayoutRepository so this mapping is unit-testable without a real database. */
fun PlacedWidget.toEntity(projectId: String) = PlacedWidgetEntity(
    projectId = projectId,
    topicKey = key,
    col = position.col,
    row = position.row,
    colSpan = position.colSpan,
    rowSpan = position.rowSpan,
)

fun PlacedWidgetEntity.toDomain() = PlacedWidget(
    key = topicKey,
    position = GridPosition(col = col, row = row, colSpan = colSpan, rowSpan = rowSpan),
)

/** Falls back to PORTRAIT for a value that isn't a current DashboardOrientation name (e.g. an old save after a rename). */
fun String.toDashboardOrientation(): DashboardOrientation =
    DashboardOrientation.entries.firstOrNull { it.name == this } ?: DashboardOrientation.PORTRAIT
