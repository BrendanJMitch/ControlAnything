package com.brendan.controlanything.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.model.ControlDef

@Composable
fun SliderWidget(
    definition: ControlDef.Slider,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${definition.displayName}: %.2f".format(value),
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = definition.min..definition.max,
        )
    }
}
