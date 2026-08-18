package com.brendan.controlanything.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.grid.GridEngine
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.domain.grid.PlacedWidget
import com.brendan.controlanything.ui.dashboard.grid.DashboardGrid
import com.brendan.controlanything.ui.dashboard.grid.gridPosition
import com.brendan.controlanything.ui.dashboard.widgets.WidgetFrame
import com.brendan.controlanything.ui.theme.ControlAnythingTheme

private data class FakeWidgetVisual(val label: String, val color: Color)

// Stand-in data until the dashboard is wired to a real DeviceInfo/DashboardViewModel in a later milestone.
private val fakeWidgetVisuals = mapOf(
    "speed" to FakeWidgetVisual("Speed", Color(0xFF6750A4)),
    "battery" to FakeWidgetVisual("Battery", Color(0xFF386A20)),
    "drive" to FakeWidgetVisual("Drive", Color(0xFFB3261E)),
    "headlights" to FakeWidgetVisual("Headlights", Color(0xFF7D5260)),
    "horn" to FakeWidgetVisual("Horn", Color(0xFF5D5F5E)),
)

private fun initialFakeWidgets() = listOf(
    PlacedWidget("speed", GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 1)),
    PlacedWidget("battery", GridPosition(col = 2, row = 0, colSpan = 1, rowSpan = 1)),
    PlacedWidget("drive", GridPosition(col = 0, row = 1, colSpan = 2, rowSpan = 2)),
    PlacedWidget("headlights", GridPosition(col = 2, row = 1, colSpan = 1, rowSpan = 1)),
    PlacedWidget("horn", GridPosition(col = 2, row = 2, colSpan = 1, rowSpan = 1)),
)

private const val DEFAULT_COLUMN_COUNT = 4
private const val MIN_COLUMN_COUNT = 2
private const val MAX_COLUMN_COUNT = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    var isEditMode by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isResizeDialogOpen by remember { mutableStateOf(false) }
    var columnCount by remember { mutableIntStateOf(DEFAULT_COLUMN_COUNT) }
    val widgets = remember { initialFakeWidgets().toMutableStateList() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isEditMode) "Done editing" else "Edit layout") },
                            onClick = {
                                isEditMode = !isEditMode
                                isMenuExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Resize grid") },
                            onClick = {
                                isResizeDialogOpen = true
                                isMenuExpanded = false
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
        ) {
            val cellSizePx = with(LocalDensity.current) { maxWidth.toPx() } / columnCount

            DashboardGrid(columnCount = columnCount, modifier = Modifier.fillMaxWidth()) {
                widgets.forEach { widget ->
                    key(widget.key) {
                        val visual = fakeWidgetVisuals.getValue(widget.key)
                        WidgetFrame(
                            isEditMode = isEditMode,
                            position = widget.position,
                            columnCount = columnCount,
                            cellSizePx = cellSizePx,
                            others = widgets.filter { it.key != widget.key },
                            onPositionChange = { newPosition ->
                                val index = widgets.indexOfFirst { it.key == widget.key }
                                if (index >= 0) widgets[index] = widgets[index].copy(position = newPosition)
                            },
                            modifier = Modifier
                                .gridPosition(widget.position)
                                .padding(4.dp)
                                .background(visual.color, RoundedCornerShape(12.dp)),
                        ) {
                            Text(
                                text = visual.label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }

    if (isResizeDialogOpen) {
        ResizeGridDialog(
            currentColumnCount = columnCount,
            onConfirm = { newColumnCount ->
                val rescaled = GridEngine.rescale(widgets.toList(), columnCount, newColumnCount)
                widgets.clear()
                widgets.addAll(rescaled)
                columnCount = newColumnCount
                isResizeDialogOpen = false
            },
            onDismiss = { isResizeDialogOpen = false },
        )
    }
}

@Composable
private fun ResizeGridDialog(
    currentColumnCount: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingColumnCount by remember { mutableIntStateOf(currentColumnCount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resize grid") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Existing widgets are rescaled to fit - you may need to fix overlaps afterward.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { pendingColumnCount = (pendingColumnCount - 1).coerceAtLeast(MIN_COLUMN_COUNT) },
                        enabled = pendingColumnCount > MIN_COLUMN_COUNT,
                    ) {
                        Text("-", style = MaterialTheme.typography.headlineSmall)
                    }
                    Text(
                        text = "$pendingColumnCount columns",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(96.dp),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(
                        onClick = { pendingColumnCount = (pendingColumnCount + 1).coerceAtMost(MAX_COLUMN_COUNT) },
                        enabled = pendingColumnCount < MAX_COLUMN_COUNT,
                    ) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pendingColumnCount) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun List<PlacedWidget>.toMutableStateList() = mutableStateListOf(*toTypedArray())

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DashboardScreenPreview() {
    ControlAnythingTheme {
        DashboardScreen()
    }
}
