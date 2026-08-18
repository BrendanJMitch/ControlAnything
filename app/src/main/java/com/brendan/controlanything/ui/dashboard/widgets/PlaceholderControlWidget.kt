package com.brendan.controlanything.ui.dashboard.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.model.ControlDef

/** Stand-in until each control type gets its real interactive widget in a later milestone. */
@Composable
fun PlaceholderControlWidget(
    definition: ControlDef,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = definition.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}
