package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.FloodViewModel
import com.example.ui.FloodWatchApp
import com.example.ui.theme.FloodWatchTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FloodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloodWatchTheme {
                FloodWatchApp(viewModel = viewModel)
            }
        }
    }
}
