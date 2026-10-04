package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.GameUiState
import com.example.model.Cloud
import com.example.model.Duck
import com.example.model.FlightDirection
import com.example.model.FloatingText
import com.example.model.Particle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    uiState: GameUiState,
    onShoot: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Native paint for crisp floating text with bold stroke outline
    val textFillPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#FFF176")
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val textStrokePaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#37474F")
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 9f
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onShoot(offset.x, offset.y)
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Sky & Sun Background
        drawSkyAndSun(canvasWidth, canvasHeight)

        // 2. Drifting Clouds
        drawClouds(uiState.clouds)

        // 3. Ducks (Flying in the sky)
        drawDucks(uiState.ducks)

        // 4. Background Hills & Foreground Grass / Wildflowers
        drawHillsAndGrass(canvasWidth, canvasHeight)

        // 5. Particles (Duck feathers & Shotgun sparks)
        drawParticles(uiState.particles)

        // 6. Floating Score Texts
        drawFloatingTexts(uiState.floatingTexts, textFillPaint, textStrokePaint)

        // 7. Shotgun Crosshair & Muzzle Blast Flash
        drawCrosshairAndShot(uiState.crosshair.x, uiState.crosshair.y, uiState.crosshair.shotAnimProgress)
    }
}

/**
 * Draws sky gradient and warm bright sun
 */
private fun DrawScope.drawSkyAndSun(width: Float, height: Float) {
    // Sky blue vertical gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF29B6F6), // Vivid sky blue top
                Color(0xFF81D4FA), // Mid sky blue
                Color(0xFFE1F5FE)  // Soft horizon blue
            ),
            startY = 0f,
            endY = height * 0.85f
        ),
        size = Size(width, height)
    )

    // Glowing warm sun in top-right
    val sunCenter = Offset(width * 0.85f, height * 0.12f)
    drawCircle(
        color = Color(0x33FFF59D),
        radius = 70f,
        center = sunCenter
    )
    drawCircle(
        color = Color(0x66FFF59D),
        radius = 48f,
        center = sunCenter
    )
    drawCircle(
        color = Color(0xFFFFF176),
        radius = 32f,
        center = sunCenter
    )
}

/**
 * Draws fluffy cartoon clouds
 */
private fun DrawScope.drawClouds(clouds: List<Cloud>) {
    for (cloud in clouds) {
        withTransform({
            translate(left = cloud.x, top = cloud.y)
            scale(scaleX = cloud.scale, scaleY = cloud.scale, pivot = Offset.Zero)
        }) {
            val cloudColor = Color(1f, 1f, 1f, cloud.alpha)
            val shadowColor = Color(0.9f, 0.94f, 0.98f, cloud.alpha)

            // Cloud base shadow
            drawCircle(color = shadowColor, radius = 28f, center = Offset(0f, 6f))
            drawCircle(color = shadowColor, radius = 38f, center = Offset(30f, -2f))
            drawCircle(color = shadowColor, radius = 26f, center = Offset(62f, 6f))

            // Cloud puff highlights
            drawCircle(color = cloudColor, radius = 28f, center = Offset(0f, 0f))
            drawCircle(color = cloudColor, radius = 38f, center = Offset(30f, -8f))
            drawCircle(color = cloudColor, radius = 26f, center = Offset(62f, 0f))
            drawRoundRect(
                color = cloudColor,
                topLeft = Offset(-18f, 0f),
                size = Size(96f, 30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f)
            )
        }
    }
}

/**
 * Draws rolling green hills and foreground grass at the bottom
 */
private fun DrawScope.drawHillsAndGrass(width: Float, height: Float) {
    val horizonY = height * 0.76f

    // 1. Distant rolling hill
    val backHillPath = Path().apply {
        moveTo(0f, horizonY + 20f)
        cubicTo(
            width * 0.3f, horizonY - 45f,
            width * 0.7f, horizonY + 15f,
            width, horizonY - 20f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = backHillPath, color = Color(0xFF66BB6A)) // Light moss green

    // 2. Foreground main grass hill
    val frontGrassY = height * 0.82f
    val frontGrassPath = Path().apply {
        moveTo(0f, frontGrassY)
        cubicTo(
            width * 0.35f, frontGrassY + 25f,
            width * 0.65f, frontGrassY - 30f,
            width, frontGrassY + 10f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = frontGrassPath, color = Color(0xFF43A047)) // Rich emerald green

    // 3. Darker bottom soil / lawn edge
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF388E3C), Color(0xFF2E7D32)),
            startY = height * 0.91f,
            endY = height
        ),
        topLeft = Offset(0f, height * 0.91f),
        size = Size(width, height * 0.09f)
    )

    // 4. Cheerful wild flowers along the grass
    val flowerColors = listOf(Color(0xFFFFEB3B), Color(0xFFFF5252), Color(0xFFFFFFFF), Color(0xFFFF9800))
    for (i in 0..10) {
        val fx = (width * 0.08f) + (i * width * 0.09f)
        val fy = frontGrassY + 18f + sin(i * 1.8f) * 12f

        // Stem
        drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(fx, fy),
            end = Offset(fx, fy - 14f),
            strokeWidth = 3f
        )
        // Petal circle
        drawCircle(
            color = flowerColors[i % flowerColors.size],
            radius = 6.5f,
            center = Offset(fx, fy - 14f)
        )
        // Center pistil
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 2.5f,
            center = Offset(fx, fy - 14f)
        )
    }
}

/**
 * Draws all ducks with animated wings, beaks, eyes, and hit effects
 */
private fun DrawScope.drawDucks(ducks: List<Duck>) {
    for (duck in ducks) {
        val isFacingRight = (duck.direction == FlightDirection.LEFT_TO_RIGHT)

        withTransform({
            translate(left = duck.x, top = duck.y)
            rotate(degrees = duck.rotation, pivot = Offset.Zero)
            // If facing right, horizontally mirror duck paths so they fly forward
            if (isFacingRight) {
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset.Zero)
            }
        }) {
            val duckAlpha = duck.alpha

            // Wing flap angle using sine wave (-30 to +25 degrees)
            val flapAngle = sin(duck.flapTimer) * 28f

            // --- Duck Tail Feathers ---
            val tailPath = Path().apply {
                moveTo(24f, -4f)
                lineTo(44f, -14f)
                lineTo(34f, 6f)
                close()
            }
            drawPath(path = tailPath, color = Color(0xFFFFD54F).copy(alpha = duckAlpha))

            // --- Duck Plump Body ---
            drawOval(
                color = Color(0xFFFFC107).copy(alpha = duckAlpha), // Golden yellow
                topLeft = Offset(-32f, -18f),
                size = Size(64f, 42f)
            )
            // Body belly shadow
            drawOval(
                color = Color(0xFFFFB300).copy(alpha = duckAlpha),
                topLeft = Offset(-26f, -6f),
                size = Size(52f, 26f)
            )

            // --- Duck Head ---
            val headCenter = Offset(-26f, -22f)
            drawCircle(
                color = Color(0xFFFFCA28).copy(alpha = duckAlpha),
                radius = 22f,
                center = headCenter
            )
            // Cheerful cheek blush
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = duckAlpha * 0.7f),
                radius = 7f,
                center = Offset(-24f, -14f)
            )

            // --- Orange Duck Bill / Beak ---
            val beakPath = Path().apply {
                moveTo(-44f, -26f)
                lineTo(-66f, -20f)
                cubicTo(-64f, -14f, -50f, -12f, -44f, -14f)
                close()
            }
            drawPath(path = beakPath, color = Color(0xFFFF9800).copy(alpha = duckAlpha))
            // Beak outline
            drawPath(
                path = beakPath,
                color = Color(0xFFE65100).copy(alpha = duckAlpha),
                style = Stroke(width = 2.5f)
            )

            // --- Duck Eye ---
            val eyeCenter = Offset(-34f, -27f)
            if (duck.isHit) {
                // Dizzy / Shot "X" Eye
                val xSize = 7f
                drawLine(
                    color = Color(0xFF212121).copy(alpha = duckAlpha),
                    start = Offset(eyeCenter.x - xSize, eyeCenter.y - xSize),
                    end = Offset(eyeCenter.x + xSize, eyeCenter.y + xSize),
                    strokeWidth = 3.5f
                )
                drawLine(
                    color = Color(0xFF212121).copy(alpha = duckAlpha),
                    start = Offset(eyeCenter.x - xSize, eyeCenter.y + xSize),
                    end = Offset(eyeCenter.x + xSize, eyeCenter.y - xSize),
                    strokeWidth = 3.5f
                )
            } else {
                // Cute lively round cartoon eye
                drawCircle(
                    color = Color.White.copy(alpha = duckAlpha),
                    radius = 6.5f,
                    center = eyeCenter
                )
                drawCircle(
                    color = Color(0xFF212121).copy(alpha = duckAlpha),
                    radius = 3.8f,
                    center = Offset(eyeCenter.x - 1.2f, eyeCenter.y)
                )
                // Eye white glimmer spark
                drawCircle(
                    color = Color.White.copy(alpha = duckAlpha),
                    radius = 1.6f,
                    center = Offset(eyeCenter.x - 2.4f, eyeCenter.y - 1.5f)
                )
            }

            // --- Animated Flapping Wing ---
            withTransform({
                translate(left = -2f, top = -10f)
                rotate(degrees = flapAngle, pivot = Offset(0f, 0f))
            }) {
                val wingPath = Path().apply {
                    moveTo(0f, 0f)
                    cubicTo(12f, -24f, 34f, -20f, 32f, 2f)
                    cubicTo(26f, 16f, 10f, 14f, 0f, 0f)
                    close()
                }
                drawPath(
                    path = wingPath,
                    color = Color(0xFFFFEE58).copy(alpha = duckAlpha)
                )
                drawPath(
                    path = wingPath,
                    color = Color(0xFFF57F17).copy(alpha = duckAlpha),
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}

/**
 * Draws particle effects (duck feathers & muzzle sparks)
 */
private fun DrawScope.drawParticles(particles: List<Particle>) {
    for (p in particles) {
        if (p.isFeather) {
            // Feather particle
            withTransform({
                translate(left = p.x, top = p.y)
                rotate(degrees = p.rotation, pivot = Offset.Zero)
            }) {
                val featherPath = Path().apply {
                    moveTo(0f, -p.size)
                    cubicTo(p.size * 0.6f, -p.size * 0.4f, p.size * 0.6f, p.size * 0.4f, 0f, p.size)
                    cubicTo(-p.size * 0.6f, p.size * 0.4f, -p.size * 0.6f, -p.size * 0.4f, 0f, -p.size)
                    close()
                }
                drawPath(path = featherPath, color = Color(p.color).copy(alpha = p.alpha))
                // Feather quill line
                drawLine(
                    color = Color(0xFFFFF9C4).copy(alpha = p.alpha),
                    start = Offset(0f, -p.size * 0.8f),
                    end = Offset(0f, p.size * 0.9f),
                    strokeWidth = 1.8f
                )
            }
        } else {
            // Shot spark / smoke
            drawCircle(
                color = Color(p.color).copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x, p.y)
            )
        }
    }
}

/**
 * Draws floating "+1" popups
 */
private fun DrawScope.drawFloatingTexts(
    floatingTexts: List<FloatingText>,
    fillPaint: Paint,
    strokePaint: Paint
) {
    for (ft in floatingTexts) {
        drawIntoCanvas(ft.x, ft.y, ft.text, ft.alpha, fillPaint, strokePaint)
    }
}

private fun DrawScope.drawIntoCanvas(
    x: Float,
    y: Float,
    text: String,
    alpha: Float,
    fillPaint: Paint,
    strokePaint: Paint
) {
    drawContext.canvas.nativeCanvas.apply {
        val originalFillAlpha = fillPaint.alpha
        val originalStrokeAlpha = strokePaint.alpha

        fillPaint.alpha = (255 * alpha).toInt().coerceIn(0, 255)
        strokePaint.alpha = (255 * alpha).toInt().coerceIn(0, 255)

        drawText(text, x, y, strokePaint)
        drawText(text, x, y, fillPaint)

        fillPaint.alpha = originalFillAlpha
        strokePaint.alpha = originalStrokeAlpha
    }
}

/**
 * Draws responsive shotgun crosshair and muzzle blast animation
 */
private fun DrawScope.drawCrosshairAndShot(
    cx: Float,
    cy: Float,
    shotAnimProgress: Float
) {
    if (cx <= 0f && cy <= 0f) return

    val targetCenter = Offset(cx, cy)

    // Shot blast muzzle ring flash (expands and fades when shot is triggered)
    if (shotAnimProgress > 0f) {
        val blastRadius = 30f + (1f - shotAnimProgress) * 75f
        val blastAlpha = shotAnimProgress.coerceIn(0f, 1f)

        // Outer blast ring
        drawCircle(
            color = Color(0xFFFF5722).copy(alpha = blastAlpha * 0.8f),
            radius = blastRadius,
            center = targetCenter,
            style = Stroke(width = 6f * blastAlpha)
        )
        // Inner white/yellow flash
        drawCircle(
            color = Color(0xFFFFEB3B).copy(alpha = blastAlpha * 0.9f),
            radius = blastRadius * 0.5f,
            center = targetCenter
        )

        // Muzzle blast radial spikes
        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180f).toFloat()
            val r1 = blastRadius * 0.4f
            val r2 = blastRadius * 1.15f
            drawLine(
                color = Color(0xFFFFEB3B).copy(alpha = blastAlpha),
                start = Offset(cx + cos(angle) * r1, cy + sin(angle) * r1),
                end = Offset(cx + cos(angle) * r2, cy + sin(angle) * r2),
                strokeWidth = 3f * blastAlpha
            )
        }
    }

    // Shotgun Targeting Crosshair
    val reticleRadius = 26f

    // Outer thin dark shadow for high contrast on light skies
    drawCircle(
        color = Color(0x66000000),
        radius = reticleRadius,
        center = targetCenter,
        style = Stroke(width = 4.5f)
    )

    // Main red crosshair circle
    drawCircle(
        color = Color(0xFFE53935),
        radius = reticleRadius,
        center = targetCenter,
        style = Stroke(width = 2.8f)
    )

    // Inner bright center dot
    drawCircle(
        color = Color(0xFFE53935),
        radius = 3.5f,
        center = targetCenter
    )

    // 4 Crosshair Tick Marks with center gap
    val gap = 10f
    val lineLen = 14f

    // Left tick
    drawLine(
        color = Color(0xFFE53935),
        start = Offset(cx - gap - lineLen, cy),
        end = Offset(cx - gap, cy),
        strokeWidth = 2.8f
    )
    // Right tick
    drawLine(
        color = Color(0xFFE53935),
        start = Offset(cx + gap, cy),
        end = Offset(cx + gap + lineLen, cy),
        strokeWidth = 2.8f
    )
    // Top tick
    drawLine(
        color = Color(0xFFE53935),
        start = Offset(cx, cy - gap - lineLen),
        end = Offset(cx, cy - gap),
        strokeWidth = 2.8f
    )
    // Bottom tick
    drawLine(
        color = Color(0xFFE53935),
        start = Offset(cx, cy + gap),
        end = Offset(cx, cy + gap + lineLen),
        strokeWidth = 2.8f
    )
}
