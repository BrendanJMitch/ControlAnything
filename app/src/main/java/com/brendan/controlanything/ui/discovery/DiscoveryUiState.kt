package com.brendan.controlanything.ui.discovery

sealed interface DiscoveryUiState {
    data object Searching : DiscoveryUiState
    data class Connecting(val label: String? = null) : DiscoveryUiState
    data class Error(val message: String) : DiscoveryUiState
}
