package com.linguaflow.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Sintetizador musical polifónico nativo (PCM 44.1 kHz con envolvente tipo marimba/campana)
 * y retroalimentación háptica para LinguaFlow.
 * Reemplaza los pitidos simples por melodías alegres y cálidas al estilo Duolingo.
 */
object SoundAndHapticsManager {

    private const val SAMPLE_RATE = 44100

    private data class NoteStep(
        val frequenciesHz: List<Double>,
        val durationMs: Int,
        val volume: Double = 0.55
    )

    /**
     * Sonido alegre al acertar: doble nota ascendente brillante (Mi5 -> Si5 + armónico Mi6)
     * con timbre cálido de marimba/campana.
     */
    fun playCorrect(context: Context) {
        vibrate(context, 35L)
        playMelodyAsync(
            listOf(
                NoteStep(frequenciesHz = listOf(587.33, 880.0), durationMs = 95, volume = 0.50),   // Re5 + La5
                NoteStep(frequenciesHz = listOf(880.0, 1174.66), durationMs = 260, volume = 0.60)  // La5 + Re6 (acorde brillante)
            )
        )
    }

    /**
     * Sonido empático al equivocarse: intervalo descendente suave y cálido (Sol4 -> Re4).
     */
    fun playWrong(context: Context) {
        vibrate(context, 85L)
        playMelodyAsync(
            listOf(
                NoteStep(frequenciesHz = listOf(392.00), durationMs = 110, volume = 0.45), // Sol4
                NoteStep(frequenciesHz = listOf(293.66), durationMs = 220, volume = 0.45)  // Re4
            )
        )
    }

    /**
     * Fanfarria alegre de celebración al completar una lección o sesión:
     * Arpegio ascendente de Do Mayor (Do5 -> Mi5 -> Sol5 -> Acorde Do6).
     */
    fun playCelebration(context: Context) {
        vibrate(context, 65L)
        playMelodyAsync(
            listOf(
                NoteStep(frequenciesHz = listOf(523.25), durationMs = 95, volume = 0.50),                  // Do5
                NoteStep(frequenciesHz = listOf(659.25), durationMs = 95, volume = 0.52),                  // Mi5
                NoteStep(frequenciesHz = listOf(783.99), durationMs = 105, volume = 0.55),                 // Sol5
                NoteStep(frequenciesHz = listOf(523.25, 659.25, 1046.50), durationMs = 380, volume = 0.65) // Acorde Do6 final
            )
        )
    }

    private fun playMelodyAsync(steps: List<NoteStep>) {
        thread(start = true, isDaemon = true, name = "LinguaFlowChimeSynth") {
            try {
                val totalSamples = steps.sumOf { (SAMPLE_RATE * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@thread
                val pcmBuffer = ShortArray(totalSamples)

                var offset = 0
                for (step in steps) {
                    val stepSamples = (SAMPLE_RATE * step.durationMs) / 1000
                    val attackSamples = (SAMPLE_RATE * 0.008).toInt().coerceAtLeast(1) // 8ms ataque suave sin clic

                    for (i in 0 until stepSamples) {
                        if (offset + i >= totalSamples) break
                        val t = i.toDouble() / SAMPLE_RATE

                        // Envolvente de ataque + caída exponencial tipo campana/marimba
                        val attackEnv = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                        val decayEnv = exp(-4.8 * (i.toDouble() / stepSamples))
                        val envelope = attackEnv * decayEnv

                        var sampleSum = 0.0
                        for (freq in step.frequenciesHz) {
                            // Fundamental + segundo armónico suave para darle brillo musical
                            val fundamental = sin(2.0 * PI * freq * t)
                            val overtone = 0.25 * sin(2.0 * PI * (freq * 2.0) * t) * exp(-8.0 * (i.toDouble() / stepSamples))
                            sampleSum += (fundamental + overtone)
                        }
                        val normalized = (sampleSum / step.frequenciesHz.size.coerceAtLeast(1)) * envelope * step.volume
                        val clamped = normalized.coerceIn(-1.0, 1.0)
                        pcmBuffer[offset + i] = (clamped * Short.MAX_VALUE).toInt().toShort()
                    }
                    offset += stepSamples
                }

                val byteCount = pcmBuffer.size * 2
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
                    .setBufferSizeInBytes(byteCount)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcmBuffer, 0, pcmBuffer.size)
                audioTrack.play()

                val totalDurationMs = steps.sumOf { it.durationMs }.toLong()
                Thread.sleep(totalDurationMs + 80L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }
}
