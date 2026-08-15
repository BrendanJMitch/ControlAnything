package com.brendan.controlanything.ui.dashboard.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.brendan.controlanything.domain.grid.GridPosition

private class GridParentData(val position: GridPosition) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@GridParentData
}

/** Marks a direct child of [DashboardGrid] with its cell coordinates (in grid units, not pixels). */
fun Modifier.gridPosition(position: GridPosition): Modifier = this.then(GridParentData(position))

/**
 * A grid with a fixed column count and square cells, sized to the available width, whose rows
 * scroll infinitely rather than being bounded. Reports its own measured height as
 * `cellSize * (deepest row used + 1)`, so it must be hosted inside a scrollable container
 * (e.g. `Modifier.verticalScroll`) rather than one that imposes a finite max height.
 */
@Composable
fun DashboardGrid(
    columnCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val cellSize = constraints.maxWidth / columnCount
        val placed = measurables.map { measurable ->
            val position = (measurable.parentData as? GridParentData)?.position
                ?: error("DashboardGrid children must use Modifier.gridPosition(...)")
            val childConstraints = Constraints.fixed(
                width = cellSize * position.colSpan,
                height = cellSize * position.rowSpan,
            )
            position to measurable.measure(childConstraints)
        }
        val maxRow = placed.maxOfOrNull { (position, _) -> position.row + position.rowSpan } ?: 0
        val height = cellSize * (maxRow + 1)
        layout(constraints.maxWidth, height) {
            placed.forEach { (position, placeable) ->
                placeable.placeRelative(x = position.col * cellSize, y = position.row * cellSize)
            }
        }
    }
}
