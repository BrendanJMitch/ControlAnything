package com.brendan.controlanything.data.layout

import com.brendan.controlanything.data.db.PlacedWidgetEntity
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.domain.model.DashboardOrientation
import org.junit.Assert.assertEquals
import org.junit.Test

class LayoutMapperTest {

    @Test
    fun `PlacedWidget round-trips through an entity unchanged`() {
        val widget = PlacedWidget("speed", GridPosition(col = 1, row = 2, colSpan = 2, rowSpan = 1))
        val entity = widget.toEntity(projectId = "proj")

        assertEquals("proj", entity.projectId)
        assertEquals("speed", entity.topicKey)
        assertEquals(1, entity.col)
        assertEquals(2, entity.row)
        assertEquals(2, entity.colSpan)
        assertEquals(1, entity.rowSpan)
        assertEquals(widget, entity.toDomain())
    }

    @Test
    fun `toDashboardOrientation parses a known value`() {
        assertEquals(DashboardOrientation.LANDSCAPE, "LANDSCAPE".toDashboardOrientation())
    }

    @Test
    fun `toDashboardOrientation falls back to PORTRAIT for an unrecognized value`() {
        assertEquals(DashboardOrientation.PORTRAIT, "SIDEWAYS".toDashboardOrientation())
    }

    @Test
    fun `entity to domain preserves the grid position exactly`() {
        val entity = PlacedWidgetEntity(
            projectId = "proj",
            topicKey = "battery",
            col = 3,
            row = 0,
            colSpan = 2,
            rowSpan = 1,
        )
        assertEquals(PlacedWidget("battery", GridPosition(3, 0, 2, 1)), entity.toDomain())
    }
}
