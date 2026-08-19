package com.brendan.controlanything.ui.dashboard.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.domain.model.ButtonMode
import com.brendan.controlanything.domain.model.ControlDef

/**
 * [onValueChange] fires per [ControlDef.Button.mode]: RISING fires true on press only, FALLING
 * fires true on release only, STATE fires true on press and false on release (or cancel, so a
 * cancelled press never leaves the app thinking the button is still held).
 */
@Composable
fun ButtonWidget(
    definition: ControlDef.Button,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource, definition.mode) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> when (definition.mode) {
                    ButtonMode.RISING, ButtonMode.STATE -> onValueChange(true)
                    ButtonMode.FALLING -> Unit
                }
                is PressInteraction.Release -> when (definition.mode) {
                    ButtonMode.FALLING -> onValueChange(true)
                    ButtonMode.STATE -> onValueChange(false)
                    ButtonMode.RISING -> Unit
                }
                is PressInteraction.Cancel -> if (definition.mode == ButtonMode.STATE) onValueChange(false)
                else -> Unit
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = {},
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(definition.displayName, style = MaterialTheme.typography.labelMedium)
        }
    }
}
