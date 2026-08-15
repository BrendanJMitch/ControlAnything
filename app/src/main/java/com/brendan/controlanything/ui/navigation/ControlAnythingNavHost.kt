package com.brendan.controlanything.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brendan.controlanything.ui.dashboard.DashboardScreen
import com.brendan.controlanything.ui.discovery.DiscoveryScreen
import kotlinx.serialization.Serializable

@Serializable
private data object Discovery

@Serializable
private data object Dashboard

@Composable
fun ControlAnythingNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Discovery) {
        composable<Discovery> {
            DiscoveryScreen(
                onDeviceReady = {
                    navController.navigate(Dashboard) {
                        popUpTo(Discovery) { inclusive = true }
                    }
                },
            )
        }
        composable<Dashboard> {
            DashboardScreen()
        }
    }
}
