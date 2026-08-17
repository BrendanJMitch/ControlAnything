package com.brendan.controlanything.ui.discovery

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brendan.controlanything.ui.theme.ControlAnythingTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DiscoveryScreen(
    onDeviceReady: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasLocalNetworkPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_LOCAL_NETWORK,
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasLocalNetworkPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasLocalNetworkPermission) {
            requestPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    LaunchedEffect(hasLocalNetworkPermission) {
        if (hasLocalNetworkPermission) {
            viewModel.startDiscovery()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToDashboard.collectLatest { onDeviceReady() }
    }

    if (hasLocalNetworkPermission) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        DiscoveryContent(state = state, modifier = modifier.fillMaxSize())
    } else {
        PermissionRequiredContent(
            onRequestPermission = { requestPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK) },
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Local network access needed", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "ControlAnything needs local network access to find and connect to your robot.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant access")
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
private fun PermissionRequiredContentPreview() {
    ControlAnythingTheme {
        PermissionRequiredContent(onRequestPermission = {}, modifier = Modifier.fillMaxSize())
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
