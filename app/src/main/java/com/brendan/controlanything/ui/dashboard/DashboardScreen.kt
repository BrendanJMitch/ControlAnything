package com.brendan.controlanything.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.grid.GridPosition
import com.brendan.controlanything.ui.dashboard.grid.DashboardGrid
import com.brendan.controlanything.ui.dashboard.grid.gridPosition
import com.brendan.controlanything.ui.theme.ControlAnythingTheme

private data class FakeWidget(val label: String, val position: GridPosition, val color: Color)

// Stand-in data until the dashboard is wired to a real DeviceInfo/DashboardViewModel in a later milestone.
private val fakeWidgets = listOf(
    FakeWidget("Speed", GridPosition(col = 0, row = 0, colSpan = 2, rowSpan = 1), Color(0xFF6750A4)),
    FakeWidget("Battery", GridPosition(col = 2, row = 0, colSpan = 1, rowSpan = 1), Color(0xFF386A20)),
    FakeWidget("Drive", GridPosition(col = 0, row = 1, colSpan = 2, rowSpan = 2), Color(0xFFB3261E)),
    FakeWidget("Headlights", GridPosition(col = 2, row = 1, colSpan = 1, rowSpan = 1), Color(0xFF7D5260)),
    FakeWidget("Horn", GridPosition(col = 2, row = 2, colSpan = 1, rowSpan = 1), Color(0xFF5D5F5E)),
)

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    DashboardGrid(
        columnCount = 4,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        fakeWidgets.forEach { widget ->
            Box(
                modifier = Modifier
                    .gridPosition(widget.position)
                    .padding(4.dp)
                    .background(widget.color, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(widget.label, color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun DashboardScreenPreview() {
    ControlAnythingTheme {
        DashboardScreen()
    }
}
