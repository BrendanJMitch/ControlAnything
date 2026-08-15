package com.brendan.controlanything

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.brendan.controlanything.ui.navigation.ControlAnythingNavHost
import com.brendan.controlanything.ui.theme.ControlAnythingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlAnythingTheme {
                ControlAnythingNavHost()
            }
        }
    }
}
