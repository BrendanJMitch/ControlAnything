package com.brendan.controlanything.domain.grid

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pure grid placement logic - deliberately free of Android/Compose imports so it can be
 * unit-tested on the JVM without instrumentation.
 */
object GridEngine {

    fun overlaps(a: GridPosition, b: GridPosition): Boolean {
        val aRight = a.col + a.colSpan
        val aBottom = a.row + a.rowSpan
        val bRight = b.col + b.colSpan
        val bBottom = b.row + b.rowSpan
        return a.col < bRight && b.col < aRight && a.row < bBottom && b.row < aBottom
    }

    /**
     * [excludingKey] lets a widget being moved/resized ignore a collision against its own
     * prior position.
     */
    fun canPlace(target: GridPosition, others: List<PlacedWidget>, excludingKey: String? = null): Boolean {
        if (target.col < 0 || target.row < 0) return false
        return others.none { it.key != excludingKey && overlaps(target, it.position) }
    }

    fun clampToGrid(target: GridPosition, columnCount: Int): GridPosition {
        val clampedColSpan = target.colSpan.coerceIn(1, columnCount)
        val clampedCol = target.col.coerceIn(0, columnCount - clampedColSpan)
        val clampedRow = target.row.coerceAtLeast(0)
        return target.copy(col = clampedCol, row = clampedRow, colSpan = clampedColSpan)
    }

    /**
     * Proportionally rescales every widget's column/colSpan to [newColumnCount]. Rows are
     * untouched since cells are square and only column resolution changes. May produce
     * overlaps - by design, the user resolves those manually afterward rather than this
     * function guaranteeing a perfect non-overlapping remap.
     */
    fun rescale(widgets: List<PlacedWidget>, oldColumnCount: Int, newColumnCount: Int): List<PlacedWidget> {
        if (oldColumnCount == newColumnCount || oldColumnCount <= 0) return widgets
        val ratio = newColumnCount.toFloat() / oldColumnCount
        return widgets.map { widget ->
            val scaledSpan = max(1, (widget.position.colSpan * ratio).roundToInt()).coerceAtMost(newColumnCount)
            val scaledCol = (widget.position.col * ratio).roundToInt().coerceIn(0, newColumnCount - scaledSpan)
            widget.copy(position = widget.position.copy(col = scaledCol, colSpan = scaledSpan))
        }
    }

    /** Finds the first unoccupied top-left cell (scanning row by row) that fits a widget of the given span. */
    fun nextFreeCell(existing: List<PlacedWidget>, columnCount: Int, colSpan: Int, rowSpan: Int): GridPosition {
        val span = colSpan.coerceIn(1, columnCount)
        var row = 0
        while (true) {
            for (col in 0..(columnCount - span)) {
                val candidate = GridPosition(col, row, span, rowSpan)
                if (existing.none { overlaps(candidate, it.position) }) return candidate
            }
            row++
        }
    }
}
