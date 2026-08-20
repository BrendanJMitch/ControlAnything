package com.brendan.controlanything.domain.grid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GridEngineTest {

    @Test
    fun `overlaps is true for identical positions`() {
        val a = GridPosition(0, 0, 2, 2)
        assertTrue(GridEngine.overlaps(a, a.copy()))
    }

    @Test
    fun `overlaps is false for adjacent non-overlapping positions`() {
        val a = GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 1)
        val b = GridPosition(col = 2, row = 0, colSpan = 2, rowSpan = 1)
        assertFalse(GridEngine.overlaps(a, b))
    }

    @Test
    fun `overlaps is true when spans partially intersect`() {
        val a = GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 2)
        val b = GridPosition(col = 1, row = 1, colSpan = 2, rowSpan = 2)
        assertTrue(GridEngine.overlaps(a, b))
    }

    @Test
    fun `overlaps is false when only touching edges`() {
        val a = GridPosition(col = 0, row = 0, colSpan = 1, rowSpan = 1)
        val b = GridPosition(col = 1, row = 0, colSpan = 1, rowSpan = 1)
        assertFalse(GridEngine.overlaps(a, b))
    }

    @Test
    fun `canPlace is false for negative coordinates`() {
        val target = GridPosition(col = -1, row = 0)
        assertFalse(GridEngine.canPlace(target, emptyList()))
    }

    @Test
    fun `canPlace is false when colliding with another widget`() {
        val existing = listOf(PlacedWidget("a", GridPosition(0, 0, 2, 2)))
        val target = GridPosition(col = 1, row = 1, colSpan = 1, rowSpan = 1)
        assertFalse(GridEngine.canPlace(target, existing))
    }

    @Test
    fun `canPlace ignores a widget's own prior position via excludingKey`() {
        val existing = listOf(PlacedWidget("a", GridPosition(0, 0, 2, 2)))
        val target = GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 2)
        assertFalse(GridEngine.canPlace(target, existing))
        assertTrue(GridEngine.canPlace(target, existing, excludingKey = "a"))
    }

    @Test
    fun `clampToGrid keeps a position that already fits`() {
        val position = GridPosition(col = 1, row = 3, colSpan = 2, rowSpan = 1)
        assertEquals(position, GridEngine.clampToGrid(position, columnCount = 4))
    }

    @Test
    fun `clampToGrid pulls a position back inside the column bounds`() {
        val position = GridPosition(col = 3, row = 0, colSpan = 2, rowSpan = 1)
        val clamped = GridEngine.clampToGrid(position, columnCount = 4)
        assertEquals(2, clamped.col)
        assertEquals(2, clamped.colSpan)
    }

    @Test
    fun `clampToGrid rejects negative rows`() {
        val position = GridPosition(col = 0, row = -5, colSpan = 1, rowSpan = 1)
        assertEquals(0, GridEngine.clampToGrid(position, columnCount = 4).row)
    }

    @Test
    fun `clampToGrid caps colSpan to the column count`() {
        val position = GridPosition(col = 0, row = 0, colSpan = 10, rowSpan = 1)
        val clamped = GridEngine.clampToGrid(position, columnCount = 4)
        assertEquals(4, clamped.colSpan)
        assertEquals(0, clamped.col)
    }

    @Test
    fun `rescale is a no-op when column count is unchanged`() {
        val widgets = listOf(PlacedWidget("a", GridPosition(1, 0, 2, 1)))
        assertEquals(widgets, GridEngine.rescale(widgets, oldColumnCount = 4, newColumnCount = 4))
    }

    @Test
    fun `rescale proportionally scales column and colSpan`() {
        val widgets = listOf(PlacedWidget("a", GridPosition(col = 2, row = 0, colSpan = 2, rowSpan = 1)))
        val rescaled = GridEngine.rescale(widgets, oldColumnCount = 4, newColumnCount = 8)
        val position = rescaled.single().position
        assertEquals(4, position.col)
        assertEquals(4, position.colSpan)
        assertEquals(0, position.row)
        assertEquals(1, position.rowSpan)
    }

    @Test
    fun `rescale never produces a zero colSpan when shrinking`() {
        val widgets = listOf(PlacedWidget("a", GridPosition(col = 0, row = 0, colSpan = 1, rowSpan = 1)))
        val rescaled = GridEngine.rescale(widgets, oldColumnCount = 8, newColumnCount = 2)
        assertTrue(rescaled.single().position.colSpan >= 1)
    }

    @Test
    fun `nextFreeCell returns the origin on an empty grid`() {
        val position = GridEngine.nextFreeCell(emptyList(), columnCount = 4, colSpan = 2, rowSpan = 1)
        assertEquals(GridPosition(0, 0, 2, 1), position)
    }

    @Test
    fun `nextFreeCell fills a gap in the current row before moving to the next row`() {
        val existing = listOf(PlacedWidget("a", GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 1)))
        val position = GridEngine.nextFreeCell(existing, columnCount = 4, colSpan = 2, rowSpan = 1)
        assertEquals(GridPosition(2, 0, 2, 1), position)
    }

    @Test
    fun `nextFreeCell moves to the next row when the current row is full`() {
        val existing = listOf(PlacedWidget("a", GridPosition(col = 0, row = 0, colSpan = 4, rowSpan = 1)))
        val position = GridEngine.nextFreeCell(existing, columnCount = 4, colSpan = 2, rowSpan = 1)
        assertEquals(GridPosition(0, 1, 2, 1), position)
    }

    @Test
    fun `reconcile keeps every saved position exactly as-is`() {
        val saved = mapOf(
            "a" to PlacedWidget("a", GridPosition(0, 0, 2, 1)),
            "b" to PlacedWidget("b", GridPosition(2, 0, 2, 1)),
        )
        val entries = listOf("a" to (2 to 1), "b" to (2 to 1))
        val result = GridEngine.reconcile(entries, saved, columnCount = 4)
        assertEquals(listOf(saved.getValue("a"), saved.getValue("b")), result)
    }

    @Test
    fun `reconcile auto-places a brand-new entry without colliding with a saved one`() {
        val saved = mapOf("existing" to PlacedWidget("existing", GridPosition(0, 0, 2, 1)))
        val entries = listOf("new" to (2 to 1), "existing" to (2 to 1))
        val result = GridEngine.reconcile(entries, saved, columnCount = 4)
        assertFalse(GridEngine.overlaps(result.first { it.key == "new" }.position, result.first { it.key == "existing" }.position))
    }

    @Test
    fun `reconcile is unaffected by whether the new entry comes before or after the saved one`() {
        val saved = mapOf("existing" to PlacedWidget("existing", GridPosition(0, 0, 2, 1)))
        val newFirst = GridEngine.reconcile(listOf("new" to (2 to 1), "existing" to (2 to 1)), saved, columnCount = 4)
        val newLast = GridEngine.reconcile(listOf("existing" to (2 to 1), "new" to (2 to 1)), saved, columnCount = 4)
        assertEquals(newFirst.toSet(), newLast.toSet())
    }

    @Test
    fun `reconcile with an empty saved map behaves like fresh auto-placement`() {
        val entries = listOf("a" to (2 to 1), "b" to (2 to 1))
        val result = GridEngine.reconcile(entries, emptyMap(), columnCount = 4)
        assertEquals(GridPosition(0, 0, 2, 1), result[0].position)
        assertEquals(GridPosition(2, 0, 2, 1), result[1].position)
    }
}
