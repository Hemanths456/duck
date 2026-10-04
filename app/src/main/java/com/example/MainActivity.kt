package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.game.GameViewModel
import com.example.ui.DuckShooterApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF29B6F6) // Sky blue background
                ) {
                    DuckShooterApp(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Automatically pause game when app loses focus or goes to background
        gameViewModel.pauseGame()
    }
}
