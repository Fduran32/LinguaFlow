package com.linguaflow.app.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Gestor multisensorial nativo de efectos de sonido y retroalimentación háptica.
 * Proporciona confirmación auditiva y táctil inmediata al acertar, fallar o completar una sesión.
 */
object SoundAndHapticsManager {

    fun playCorrect(context: Context) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 140)
            vibrate(context, 35L)
        } catch (_: Exception) {
        }
    }

    fun playWrong(context: Context) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 200)
            vibrate(context, 90L)
        } catch (_: Exception) {
        }
    }

    fun playCelebration(context: Context) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 320)
            vibrate(context, 60L)
        } catch (_: Exception) {
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
