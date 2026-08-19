package com.brendan.controlanything.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.model.ControlDef
import com.brendan.controlanything.domain.model.SliderOrientation

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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${definition.displayName}: %.2f".format(value),
            style = MaterialTheme.typography.labelMedium,
        )
        when (definition.orientation) {
            SliderOrientation.HORIZONTAL -> Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = definition.min..definition.max,
                modifier = Modifier.fillMaxWidth(),
            )
            SliderOrientation.VERTICAL -> VerticalSlider(
                value = value,
                onValueChange = onValueChange,
                valueRange = definition.min..definition.max,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Material3 only ships a horizontal Slider. This measures it with swapped constraints, then
 * rotates it 270 degrees around its own top-left corner - the standard Compose recipe for a
 * vertical slider, since graphicsLayer transforms are honored by pointer-input hit testing too.
 * Top of the track ends up as the max value, bottom as the min, matching a volume-fader feel.
 */
@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth,
                    ),
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(x = 0, y = placeable.width)
                }
            }
            .graphicsLayer {
                rotationZ = 270f
                transformOrigin = TransformOrigin(0f, 0f)
            },
    )
}
