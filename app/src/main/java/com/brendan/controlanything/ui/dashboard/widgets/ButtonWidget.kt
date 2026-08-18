package com.brendan.controlanything.ui.dashboard.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.model.ControlDef

@Composable
fun ButtonWidget(
    definition: ControlDef.Button,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Button(onClick = onPress, modifier = Modifier.fillMaxSize()) {
            Text(definition.displayName, style = MaterialTheme.typography.labelMedium)
        }
    }
}
