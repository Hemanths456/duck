package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural retro sound manager that synthesizes audio without external sound files.
 * Zero asset dependencies, 100% reliable, low-latency, and safe across all Android devices.
 */
class SoundManager {

    companion object {
        private const val TAG = "SoundManager"
        private const val SAMPLE_RATE = 44100
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var isMuted = false

    // Pre-synthesized PCM buffers
    private var shotSoundData: ByteArray? = null
    private var hitSoundData: ByteArray? = null
    private var gameOverSoundData: ByteArray? = null
    private var clickSoundData: ByteArray? = null

    init {
        audioScope.launch {
            try {
                shotSoundData = synthesizeShotgun()
                hitSoundData = synthesizeDuckHit()
                gameOverSoundData = synthesizeGameOver()
                clickSoundData = synthesizeClick()
            } catch (e: Exception) {
                Log.w(TAG, "Audio synthesis failed gracefully", e)
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isMuted = !enabled
    }

    fun isSoundEnabled(): Boolean = !isMuted

    fun playShot() {
        if (isMuted) return
        shotSoundData?.let { playBuffer(it) }
    }

    fun playDuckHit() {
        if (isMuted) return
        hitSoundData?.let { playBuffer(it) }
    }

    fun playGameOver() {
        if (isMuted) return
        gameOverSoundData?.let { playBuffer(it) }
    }

    fun playClick() {
        if (isMuted) return
        clickSoundData?.let { playBuffer(it) }
    }

    private fun playBuffer(data: ByteArray) {
        audioScope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(data.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(data, 0, data.size)
                audioTrack.play()

                // Release track after completion
                val durationMs = (data.size / 2 * 1000L) / SAMPLE_RATE
                kotlinx.coroutines.delay(durationMs + 50L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Silently ignore audio playback errors
            }
        }
    }

    /**
     * Synthesize a punchy arcade shotgun blast (white noise decay + low-frequency thump)
     */
    private fun synthesizeShotgun(): ByteArray {
        val durationSec = 0.12f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ByteArray(numSamples * 2)
        val random = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            // Fast exponential decay envelope
            val env = exp(-t * 35.0).toFloat()
            // White noise component
            val noise = (random.nextFloat() * 2f - 1f) * 0.75f
            // Bass thump: starts at 150Hz dropping to 40Hz
            val freq = (150f - t * 800f).coerceAtLeast(35f)
            val bass = sin(2f * PI.toFloat() * freq * t) * 0.85f

            val sample = ((noise + bass) * env).coerceIn(-1f, 1f)
            val pcm = (sample * 32767).toInt().toShort()

            buffer[i * 2] = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    /**
     * Synthesize a funny cartoon duck quack / comic hit sound
     */
    private fun synthesizeDuckHit(): ByteArray {
        val durationSec = 0.18f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val progress = t / durationSec
            // Quack envelope: attack then decay
            val env = if (progress < 0.2f) progress / 0.2f else 1f - ((progress - 0.2f) / 0.8f)

            // Duck quack pitch bend: bends upward then downward
            val bend = sin(progress * PI.toFloat()) * 180f
            val f1 = 620f + bend
            val f2 = 830f + bend

            val tone1 = sin(2f * PI.toFloat() * f1 * t)
            val tone2 = sin(2f * PI.toFloat() * f2 * t) * 0.5f

            val sample = ((tone1 + tone2) * 0.6f * env).coerceIn(-1f, 1f)
            val pcm = (sample * 32767).toInt().toShort()

            buffer[i * 2] = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    /**
     * Synthesize a classic descending retro game-over jingle (G4 -> E4 -> C4 -> G3)
     */
    private fun synthesizeGameOver(): ByteArray {
        val noteFreqs = floatArrayOf(392.00f, 329.63f, 261.63f, 196.00f)
        val noteDuration = 0.14f
        val totalSec = noteDuration * noteFreqs.size
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val noteIndex = (t / noteDuration).toInt().coerceIn(0, noteFreqs.size - 1)
            val noteT = t - (noteIndex * noteDuration)
            val env = (1f - (noteT / noteDuration)).coerceIn(0f, 1f)
            val freq = noteFreqs[noteIndex]

            // Retro square/triangle-ish wave
            val sine = sin(2f * PI.toFloat() * freq * t)
            val harmonic = sin(4f * PI.toFloat() * freq * t) * 0.3f
            val sample = ((sine + harmonic) * 0.5f * env).coerceIn(-1f, 1f)
            val pcm = (sample * 32767).toInt().toShort()

            buffer[i * 2] = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    /**
     * Synthesize a short crisp UI click
     */
    private fun synthesizeClick(): ByteArray {
        val durationSec = 0.035f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val env = 1f - (t / durationSec)
            val sample = (sin(2f * PI.toFloat() * 950f * t) * 0.45f * env).coerceIn(-1f, 1f)
            val pcm = (sample * 32767).toInt().toShort()

            buffer[i * 2] = (pcm.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }
}
