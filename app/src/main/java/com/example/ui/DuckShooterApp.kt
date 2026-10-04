package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameViewModel
import com.example.model.GameScreenState
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameHud
import com.example.ui.components.GameOverDialog
import com.example.ui.components.PauseDialog
import com.example.ui.components.StartScreen
import kotlinx.coroutines.isActive

@Composable
fun DuckShooterApp(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle Android system back button properly per screen state
    BackHandler {
        when (uiState.screenState) {
            GameScreenState.PLAYING -> viewModel.pauseGame()
            GameScreenState.PAUSED -> viewModel.resumeGame()
            GameScreenState.GAME_OVER -> viewModel.goToMainMenu()
            GameScreenState.START -> { /* Let system exit app */ }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Initialize clouds with actual screen dimensions
        LaunchedEffect(screenWidth, screenHeight) {
            if (screenWidth > 0f && screenHeight > 0f) {
                viewModel.initClouds(screenWidth, screenHeight)
            }
        }

        // Smooth frame-synced game loop (runs only while PLAYING)
        LaunchedEffect(uiState.screenState, screenWidth, screenHeight) {
            if (uiState.screenState == GameScreenState.PLAYING && screenWidth > 0f && screenHeight > 0f) {
                var lastTime = withFrameNanos { it }
                while (isActive) {
                    withFrameNanos { frameTime ->
                        val dt = (frameTime - lastTime) / 1_000_000_000f
                        lastTime = frameTime
                        viewModel.updateGame(dt, screenWidth, screenHeight)
                    }
                }
            }
        }

        when (uiState.screenState) {
            GameScreenState.START -> {
                StartScreen(
                    bestScore = uiState.bestScore,
                    isSoundEnabled = uiState.isSoundEnabled,
                    onPlayClick = { viewModel.startGame() },
                    onToggleSound = { viewModel.toggleSound() }
                )
            }

            GameScreenState.PLAYING,
            GameScreenState.PAUSED,
            GameScreenState.GAME_OVER -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Game World (Sky, Ducks, Grass, Particles, Crosshair)
                    GameCanvas(
                        uiState = uiState,
                        onShoot = { x, y -> viewModel.onShoot(x, y) }
                    )

                    // Top Heads-Up Display (Score, Lives, Pause)
                    GameHud(
                        score = uiState.score,
                        level = uiState.level,
                        lives = uiState.lives,
                        showGreatShot = uiState.showGreatShot,
                        showDuckEscaped = uiState.showDuckEscaped,
                        onPauseClick = { viewModel.pauseGame() }
                    )

                    // Pause Overlay Dialog
                    if (uiState.screenState == GameScreenState.PAUSED) {
                        PauseDialog(
                            onResume = { viewModel.resumeGame() },
                            onRestart = { viewModel.restartGame() },
                            onMainMenu = { viewModel.goToMainMenu() },
                            isSoundEnabled = uiState.isSoundEnabled,
                            onToggleSound = { viewModel.toggleSound() }
                        )
                    }

                    // Game Over Overlay Dialog
                    if (uiState.screenState == GameScreenState.GAME_OVER) {
                        GameOverDialog(
                            score = uiState.score,
                            bestScore = uiState.bestScore,
                            isNewBestScore = uiState.isNewBestScore,
                            onPlayAgain = { viewModel.restartGame() },
                            onMainMenu = { viewModel.goToMainMenu() }
                        )
                    }
                }
            }
        }
    }
}
