package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.SoundManager
import com.example.data.PreferencesManager
import com.example.model.Cloud
import com.example.model.CrosshairState
import com.example.model.Duck
import com.example.model.FlightDirection
import com.example.model.FloatingText
import com.example.model.GameScreenState
import com.example.model.Particle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.math.sin

data class GameUiState(
    val screenState: GameScreenState = GameScreenState.START,
    val score: Int = 0,
    val bestScore: Int = 0,
    val lives: Int = GameConstants.INITIAL_LIVES,
    val level: Int = 1,
    val ducks: List<Duck> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val floatingTexts: List<FloatingText> = emptyList(),
    val clouds: List<Cloud> = emptyList(),
    val crosshair: CrosshairState = CrosshairState(x = 0f, y = 0f),
    val showGreatShot: Boolean = false,
    val showDuckEscaped: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isNewBestScore: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application.applicationContext)
    val soundManager = SoundManager()

    private val _uiState = MutableStateFlow(
        GameUiState(
            bestScore = prefs.bestScore,
            isSoundEnabled = prefs.isSoundEnabled
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val random = Random()
    private var nextEntityId = 1L
    private var spawnTimer = 0.5f
    private var greatShotTimer = 0f
    private var duckEscapedTimer = 0f
    private var cloudsInitialized = false

    init {
        soundManager.setSoundEnabled(prefs.isSoundEnabled)
    }

    /**
     * Initializes drifting background clouds based on measured screen dimensions.
     */
    fun initClouds(screenWidth: Float, screenHeight: Float) {
        if (cloudsInitialized || screenWidth <= 0 || screenHeight <= 0) return
        cloudsInitialized = true

        val cloudList = listOf(
            Cloud(id = 1, x = screenWidth * 0.15f, y = screenHeight * 0.08f, speed = 18f, scale = 1.0f, alpha = 0.85f),
            Cloud(id = 2, x = screenWidth * 0.70f, y = screenHeight * 0.16f, speed = 25f, scale = 1.25f, alpha = 0.90f),
            Cloud(id = 3, x = screenWidth * 0.42f, y = screenHeight * 0.26f, speed = 14f, scale = 0.85f, alpha = 0.75f),
            Cloud(id = 4, x = screenWidth * 0.95f, y = screenHeight * 0.05f, speed = 20f, scale = 0.95f, alpha = 0.80f)
        )
        _uiState.value = _uiState.value.copy(
            clouds = cloudList,
            crosshair = CrosshairState(x = screenWidth / 2f, y = screenHeight / 2f)
        )
    }

    fun startGame() {
        soundManager.playClick()
        resetGameSession()
        _uiState.value = _uiState.value.copy(screenState = GameScreenState.PLAYING)
    }

    fun pauseGame() {
        if (_uiState.value.screenState == GameScreenState.PLAYING) {
            soundManager.playClick()
            _uiState.value = _uiState.value.copy(screenState = GameScreenState.PAUSED)
        }
    }

    fun resumeGame() {
        if (_uiState.value.screenState == GameScreenState.PAUSED) {
            soundManager.playClick()
            _uiState.value = _uiState.value.copy(screenState = GameScreenState.PLAYING)
        }
    }

    fun restartGame() {
        soundManager.playClick()
        resetGameSession()
        _uiState.value = _uiState.value.copy(screenState = GameScreenState.PLAYING)
    }

    fun goToMainMenu() {
        soundManager.playClick()
        _uiState.value = _uiState.value.copy(
            screenState = GameScreenState.START,
            ducks = emptyList(),
            particles = emptyList(),
            floatingTexts = emptyList()
        )
    }

    fun toggleSound() {
        val newSoundState = !_uiState.value.isSoundEnabled
        prefs.isSoundEnabled = newSoundState
        soundManager.setSoundEnabled(newSoundState)
        _uiState.value = _uiState.value.copy(isSoundEnabled = newSoundState)
        if (newSoundState) {
            soundManager.playClick()
        }
    }

    private fun resetGameSession() {
        spawnTimer = 0.6f
        greatShotTimer = 0f
        duckEscapedTimer = 0f
        _uiState.value = _uiState.value.copy(
            score = 0,
            lives = GameConstants.INITIAL_LIVES,
            level = 1,
            ducks = emptyList(),
            particles = emptyList(),
            floatingTexts = emptyList(),
            showGreatShot = false,
            showDuckEscaped = false,
            isNewBestScore = false
        )
    }

    /**
     * Primary touch handling: shoots shotgun at tap location.
     */
    fun onShoot(touchX: Float, touchY: Float) {
        if (_uiState.value.screenState != GameScreenState.PLAYING) return

        // 1. Play shotgun sound
        soundManager.playShot()

        // 2. Trigger crosshair shot flash animation
        val updatedCrosshair = CrosshairState(
            x = touchX,
            y = touchY,
            isShotActive = true,
            shotAnimProgress = 1.0f
        )

        // 3. Create shotgun muzzle smoke / sparks at touch location
        val newParticles = mutableListOf<Particle>()
        for (i in 0 until 6) {
            val angle = random.nextFloat() * (Math.PI * 2).toFloat()
            val speed = 80f + random.nextFloat() * 120f
            newParticles.add(
                Particle(
                    id = nextEntityId++,
                    x = touchX,
                    y = touchY,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = 4f + random.nextFloat() * 5f,
                    color = if (i % 2 == 0) 0xFFFFEE58 else 0xFFFF7043,
                    life = 0.35f,
                    maxLife = 0.35f
                )
            )
        }

        // 4. Hit detection: check if any alive duck was hit
        val currentDucks = _uiState.value.ducks.map { it.copy() }
        var hitAnyDuck = false
        var hitDuckX = 0f
        var hitDuckY = 0f

        for (duck in currentDucks) {
            if (!duck.isHit && duck.containsPoint(touchX, touchY, GameConstants.TOUCH_HIT_TOLERANCE)) {
                // Duck is shot!
                duck.isHit = true
                duck.hitTime = System.currentTimeMillis()
                duck.velocityY = -220f // satisfying little hop upward
                duck.rotation = if (duck.direction == FlightDirection.LEFT_TO_RIGHT) 25f else -25f

                hitDuckX = duck.x
                hitDuckY = duck.y
                hitAnyDuck = true
                break // One shot hits one duck
            }
        }

        val updatedFloatingTexts = _uiState.value.floatingTexts.toMutableList()

        if (hitAnyDuck) {
            // Play cartoon quack / hit sound
            soundManager.playDuckHit()

            // Spawn cartoon feathers explosion
            for (i in 0 until 12) {
                val angle = random.nextFloat() * (Math.PI * 2).toFloat()
                val speed = 120f + random.nextFloat() * 240f
                val isYellow = random.nextBoolean()
                newParticles.add(
                    Particle(
                        id = nextEntityId++,
                        x = hitDuckX,
                        y = hitDuckY,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed - 60f,
                        size = 12f + random.nextFloat() * 10f,
                        color = if (isYellow) 0xFFFFD54F else 0xFFFFF9C4,
                        life = 0.85f,
                        maxLife = 0.85f,
                        rotation = random.nextFloat() * 360f,
                        rotationSpeed = (random.nextFloat() * 360f - 180f),
                        isFeather = true
                    )
                )
            }

            // Spawn floating "+1" text
            updatedFloatingTexts.add(
                FloatingText(
                    id = nextEntityId++,
                    text = "+${GameConstants.POINTS_PER_DUCK}",
                    x = hitDuckX,
                    y = hitDuckY - 30f,
                    life = 0.9f,
                    maxLife = 0.9f,
                    color = 0xFFFFEB3B
                )
            )

            // Update Score & Level
            val newScore = _uiState.value.score + GameConstants.POINTS_PER_DUCK
            val newLevel = (newScore / GameConstants.POINTS_PER_LEVEL) + 1
            var isNewBest = false
            var currentBest = _uiState.value.bestScore

            if (newScore > currentBest) {
                currentBest = newScore
                prefs.bestScore = newScore
                isNewBest = true
            }

            var triggerGreatShot = false
            if (newScore > 0 && newScore % GameConstants.GREAT_SHOT_INTERVAL == 0) {
                triggerGreatShot = true
                greatShotTimer = GameConstants.GREAT_SHOT_DURATION_SEC
            }

            _uiState.value = _uiState.value.copy(
                score = newScore,
                level = newLevel,
                bestScore = currentBest,
                isNewBestScore = isNewBest,
                showGreatShot = triggerGreatShot || _uiState.value.showGreatShot,
                ducks = currentDucks,
                particles = _uiState.value.particles + newParticles,
                floatingTexts = updatedFloatingTexts,
                crosshair = updatedCrosshair
            )
        } else {
            // Shot missed
            _uiState.value = _uiState.value.copy(
                particles = _uiState.value.particles + newParticles,
                crosshair = updatedCrosshair
            )
        }
    }

    /**
     * Updates game state per frame (called from Composable LaunchedEffect loop).
     */
    fun updateGame(dt: Float, screenWidth: Float, screenHeight: Float) {
        if (_uiState.value.screenState != GameScreenState.PLAYING) return
        if (screenWidth <= 0 || screenHeight <= 0) return

        // Clamp dt to prevent teleportation during lag spikes
        val clampedDt = dt.coerceIn(0f, 0.05f)

        // 1. Update Clouds (wrap around screen)
        val updatedClouds = _uiState.value.clouds.map { cloud ->
            var newX = cloud.x + cloud.speed * clampedDt
            if (newX > screenWidth + 150f) {
                newX = -150f
            }
            cloud.copy(x = newX)
        }

        // 2. Update Great Shot banner timer
        var showGreatShot = _uiState.value.showGreatShot
        if (showGreatShot) {
            greatShotTimer -= clampedDt
            if (greatShotTimer <= 0f) {
                showGreatShot = false
            }
        }

        // 3. Update Duck Escaped banner timer
        var showDuckEscaped = _uiState.value.showDuckEscaped
        if (showDuckEscaped) {
            duckEscapedTimer -= clampedDt
            if (duckEscapedTimer <= 0f) {
                showDuckEscaped = false
            }
        }

        // 4. Update Crosshair shot animation
        val crosshair = _uiState.value.crosshair
        val updatedCrosshair = if (crosshair.isShotActive) {
            val newProgress = crosshair.shotAnimProgress - (clampedDt * 4.5f)
            if (newProgress <= 0f) {
                crosshair.copy(isShotActive = false, shotAnimProgress = 0f)
            } else {
                crosshair.copy(shotAnimProgress = newProgress)
            }
        } else {
            crosshair
        }

        // 5. Update Particles
        val updatedParticles = _uiState.value.particles.mapNotNull { p ->
            val newLife = p.life - clampedDt
            if (newLife <= 0f) {
                null
            } else {
                val newX = p.x + p.vx * clampedDt
                val newVy = if (p.isFeather) p.vy + 260f * clampedDt else p.vy + 400f * clampedDt
                val newY = p.y + newVy * clampedDt
                val newAlpha = (newLife / p.maxLife).coerceIn(0f, 1f)
                val newRot = p.rotation + p.rotationSpeed * clampedDt
                p.copy(
                    x = newX,
                    y = newY,
                    vy = newVy,
                    alpha = newAlpha,
                    rotation = newRot,
                    life = newLife
                )
            }
        }

        // 6. Update Floating Texts
        val updatedFloatingTexts = _uiState.value.floatingTexts.mapNotNull { ft ->
            val newLife = ft.life - clampedDt
            if (newLife <= 0f) {
                null
            } else {
                val newY = ft.y + ft.vy * clampedDt
                val newAlpha = (newLife / ft.maxLife).coerceIn(0f, 1f)
                ft.copy(y = newY, alpha = newAlpha, life = newLife)
            }
        }

        // 7. Update Ducks & Check Escapes
        var currentLives = _uiState.value.lives
        var duckJustEscaped = false
        val remainingDucks = mutableListOf<Duck>()

        for (duck in _uiState.value.ducks) {
            if (duck.isHit) {
                // Falling hit animation
                duck.velocityY += 750f * clampedDt // Gravity
                duck.y += duck.velocityY * clampedDt
                duck.rotation += if (duck.direction == FlightDirection.LEFT_TO_RIGHT) 380f * clampedDt else -380f * clampedDt
                duck.alpha = (duck.alpha - clampedDt * 2.2f).coerceAtLeast(0f)

                // Retain duck while falling, remove when faded or fell past grass
                if (duck.alpha > 0f && duck.y < screenHeight) {
                    remainingDucks.add(duck)
                }
            } else {
                // Normal alive flight
                duck.flightTime += clampedDt
                duck.x += duck.speedX * clampedDt
                duck.flapTimer += clampedDt * GameConstants.WING_FLAP_SPEED

                // Sine wave vertical bobbing
                duck.y = duck.baseY + sin(duck.flightTime * GameConstants.DUCK_BOB_FREQUENCY) * GameConstants.DUCK_BOB_AMPLITUDE

                // Check if duck reached the other side without being shot
                val isOffScreen = when (duck.direction) {
                    FlightDirection.LEFT_TO_RIGHT -> duck.x > (screenWidth + duck.width)
                    FlightDirection.RIGHT_TO_LEFT -> duck.x < (-duck.width)
                }

                if (isOffScreen) {
                    // Duck escaped! Player loses 1 life!
                    currentLives -= 1
                    duckJustEscaped = true
                } else {
                    remainingDucks.add(duck)
                }
            }
        }

        if (duckJustEscaped) {
            duckEscapedTimer = GameConstants.ESCAPED_ALERT_DURATION_SEC
            showDuckEscaped = true
        }

        // 8. Check Game Over
        if (currentLives <= 0) {
            currentLives = 0
            soundManager.playGameOver()
            _uiState.value = _uiState.value.copy(
                screenState = GameScreenState.GAME_OVER,
                lives = 0,
                ducks = remainingDucks,
                particles = updatedParticles,
                floatingTexts = updatedFloatingTexts,
                clouds = updatedClouds,
                crosshair = updatedCrosshair,
                showGreatShot = false,
                showDuckEscaped = false
            )
            return
        }

        // 9. Spawning Logic based on Level and Active Ducks
        val activeAliveDucks = remainingDucks.count { !it.isHit }
        val currentLevel = _uiState.value.level

        // Max ducks permitted: Level 1 -> 1 duck; Level 2-3 -> 2 ducks; Level 4+ -> 3 ducks
        val maxAllowedDucks = when {
            currentLevel >= 4 -> GameConstants.MAX_DUCKS_ON_SCREEN
            currentLevel >= 2 -> 2
            else -> 1
        }

        spawnTimer -= clampedDt
        if (activeAliveDucks < maxAllowedDucks && spawnTimer <= 0f) {
            // Spawn a new duck!
            val direction = if (random.nextBoolean()) FlightDirection.LEFT_TO_RIGHT else FlightDirection.RIGHT_TO_LEFT
            val calculatedSpeed = (GameConstants.BASE_DUCK_SPEED + (currentLevel - 1) * GameConstants.SPEED_INCREASE_PER_LEVEL)
                .coerceAtMost(GameConstants.MAX_DUCK_SPEED) + (random.nextFloat() * 40f - 20f)

            val speedX = if (direction == FlightDirection.LEFT_TO_RIGHT) calculatedSpeed else -calculatedSpeed
            val spawnX = if (direction == FlightDirection.LEFT_TO_RIGHT) -100f else screenWidth + 100f

            // Flight altitude: between 14% and 52% of screen height
            val minY = screenHeight * 0.14f
            val maxY = screenHeight * 0.52f
            val spawnY = minY + random.nextFloat() * (maxY - minY)

            val newDuck = Duck(
                id = nextEntityId++,
                x = spawnX,
                y = spawnY,
                speedX = speedX,
                baseY = spawnY,
                direction = direction,
                flapTimer = random.nextFloat() * 6.28f,
                flightTime = random.nextFloat() * 6.28f
            )
            remainingDucks.add(newDuck)

            // Reset spawn timer with random variance
            val baseInterval = (GameConstants.MAX_SPAWN_INTERVAL_SEC - (currentLevel * 0.15f))
                .coerceAtLeast(GameConstants.MIN_SPAWN_INTERVAL_SEC)
            spawnTimer = baseInterval + (random.nextFloat() * 0.6f)
        }

        _uiState.value = _uiState.value.copy(
            lives = currentLives,
            ducks = remainingDucks,
            particles = updatedParticles,
            floatingTexts = updatedFloatingTexts,
            clouds = updatedClouds,
            crosshair = updatedCrosshair,
            showGreatShot = showGreatShot,
            showDuckEscaped = showDuckEscaped
        )
    }
}
