package com.example.model

enum class GameScreenState {
    START,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class FlightDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

data class Duck(
    val id: Long,
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float = 0f,
    val baseY: Float,
    val direction: FlightDirection,
    val width: Float = 110f,
    val height: Float = 85f,
    var flapTimer: Float = 0f,
    var flightTime: Float = 0f,
    var isHit: Boolean = false,
    var hitTime: Long = 0L,
    var rotation: Float = 0f,
    var velocityY: Float = 0f,
    var alpha: Float = 1f
) {
    /**
     * Hitbox with generous tolerance for comfortable mobile touch interaction.
     */
    fun containsPoint(px: Float, py: Float, touchRadius: Float = 40f): Boolean {
        if (isHit) return false
        val halfW = (width / 2f) + touchRadius
        val halfH = (height / 2f) + touchRadius
        return px >= (x - halfW) && px <= (x + halfW) &&
               py >= (y - halfH) && py <= (y + halfH)
    }
}

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Long,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var life: Float = 1f,
    val maxLife: Float = 1f,
    val isFeather: Boolean = false
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float = 1f,
    var vy: Float = -120f,
    var life: Float = 1f,
    val maxLife: Float = 1f,
    val color: Long = 0xFFFFD700
)

data class Cloud(
    val id: Int,
    var x: Float,
    var y: Float,
    var speed: Float,
    var scale: Float,
    var alpha: Float = 0.85f
)

data class CrosshairState(
    val x: Float,
    val y: Float,
    val isShotActive: Boolean = false,
    val shotAnimProgress: Float = 0f
)
