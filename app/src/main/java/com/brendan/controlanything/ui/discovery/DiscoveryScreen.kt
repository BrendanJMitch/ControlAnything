package com.brendan.controlanything.ui.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brendan.controlanything.ui.theme.ControlAnythingTheme

@Composable
fun DiscoveryScreen(
    onDeviceReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Temporary stand-in for DiscoveryViewModel until NSD/MQTT land in a later milestone -
    // the debug button below drives the same state transitions the real flow will produce.
    var state by remember { mutableStateOf<DiscoveryUiState>(DiscoveryUiState.Searching) }

    Box(modifier = modifier.fillMaxSize()) {
        DiscoveryContent(state = state, modifier = Modifier.fillMaxSize())
        TextButton(
            onClick = {
                state = when (state) {
                    is DiscoveryUiState.Searching -> DiscoveryUiState.Connecting(label = "Rover 2")
                    else -> {
                        onDeviceReady()
                        state
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            Text("Debug: advance")
        }
    }
}

@Composable
private fun DiscoveryContent(
    state: DiscoveryUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (state) {
            is DiscoveryUiState.Searching -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(24.dp))
                Text("Searching for your robot...", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Make sure your phone is connected to your robot's Wi-Fi network.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            is DiscoveryUiState.Connecting -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(24.dp))
                Text(
                    text = state.label?.let { "Connecting to $it..." } ?: "Connecting...",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            is DiscoveryUiState.Error -> {
                Text("Couldn't connect", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryContentSearchingPreview() {
    ControlAnythingTheme {
        DiscoveryContent(state = DiscoveryUiState.Searching, modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryContentConnectingPreview() {
    ControlAnythingTheme {
        DiscoveryContent(state = DiscoveryUiState.Connecting(label = "Rover 2"), modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryContentErrorPreview() {
    ControlAnythingTheme {
        DiscoveryContent(
            state = DiscoveryUiState.Error("Lost connection to the broker."),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
