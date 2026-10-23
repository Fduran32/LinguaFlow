package com.linguaflow.app.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
import kotlin.math.max

class SpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    var isListening: Boolean = false
        private set

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        isListening = false
    }

    /**
     * Inicia la escucha de voz de manera 100% interna (headless) sin lanzar diálogos ni popups de Google.
     */
    fun startListening(
        localeStr: String = "en-US",
        onReady: () -> Unit = {},
        onRmsChanged: (Float) -> Unit = {},
        onPartialResult: (String) -> Unit = {},
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val action = {
            try {
                stopListeningInternal()

                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onReady()
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // rmsdB suele ir de -2 a 10 aprox. Normalizamos a 0.0 - 1.0 para la animación visual
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onRmsChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna frase. Intenta hablar más claro."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó audio a tiempo."
                            SpeechRecognizer.ERROR_AUDIO -> "Error de grabación de audio."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Se requiere permiso de micrófono."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Verifica tu conexión para reconocimiento de voz."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El micrófono está ocupado. Intenta de nuevo."
                            else -> "Intenta pronunciar de nuevo."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognized = matches?.firstOrNull() ?: ""
                        onFinalResult(recognized)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val recognized = matches?.firstOrNull() ?: ""
                        if (recognized.isNotBlank()) {
                            onPartialResult(recognized)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeStr)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeStr)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeStr)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                recognizer.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                onError("No se pudo iniciar el micrófono: ${e.localizedMessage}")
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun stopListening() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            stopListeningInternal()
        } else {
            mainHandler.post { stopListeningInternal() }
        }
    }

    fun destroy() {
        stopListening()
    }

    companion object {
        /**
         * Compara el texto hablado con la frase objetivo.
         * Devuelve un valor de precisión entre 0 y 100.
         */
        fun calculateSimilarity(spoken: String, target: String): SimilarityResult {
            val cleanSpoken = cleanText(spoken)
            val cleanTarget = cleanText(target)

            if (cleanSpoken.isEmpty()) {
                return SimilarityResult(0, false, "No se escuchó ninguna palabra.")
            }

            if (cleanSpoken == cleanTarget) {
                return SimilarityResult(100, true, "¡Pronunciación perfecta! 🌟")
            }

            // Comparación de palabras clave
            val targetWords = cleanTarget.split(" ").filter { it.isNotBlank() }
            val spokenWords = cleanSpoken.split(" ").filter { it.isNotBlank() }

            var matchedWordsCount = 0
            val missingWords = mutableListOf<String>()

            for (targetWord in targetWords) {
                if (spokenWords.any { it.equals(targetWord, ignoreCase = true) || levenshtein(it, targetWord) <= 1 }) {
                    matchedWordsCount++
                } else {
                    missingWords.add(targetWord)
                }
            }

            val wordScore = if (targetWords.isNotEmpty()) (matchedWordsCount * 100) / targetWords.size else 0
            val levDistance = levenshtein(cleanSpoken, cleanTarget)
            val maxLen = max(cleanSpoken.length, cleanTarget.length)
            val levScore = if (maxLen > 0) ((maxLen - levDistance) * 100) / maxLen else 0

            val finalScore = ((wordScore * 0.6) + (levScore * 0.4)).toInt().coerceIn(0, 100)
            val isPassed = finalScore >= 65

            val feedback = when {
                finalScore >= 90 -> "¡Excelente pronunciación! 🌟"
                finalScore >= 75 -> "¡Muy bien! Se entendió casi a la perfección 👍"
                finalScore >= 60 -> if (missingWords.isNotEmpty()) {
                    "Casi lo logras. Intenta articular mejor: \"${missingWords.take(2).joinToString(", ")}\""
                } else {
                    "Se entendió la idea general. ¡Buen intento!"
                }
                else -> "Intenta pronunciar más claro y pausado."
            }

            return SimilarityResult(finalScore, isPassed, feedback)
        }

        private fun cleanText(text: String): String {
            return text.lowercase(Locale.ROOT)
                .replace(Regex("[.,?!;:'\"¿¡-]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
            val lhsLen = lhs.length
            val rhsLen = rhs.length
            var cost = IntArray(lhsLen + 1) { it }
            var newCost = IntArray(lhsLen + 1) { 0 }

            for (i in 1..rhsLen) {
                newCost[0] = i
                for (j in 1..lhsLen) {
                    val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1
                    newCost[j] = minOf(costInsert, costDelete, costReplace)
                }
                val swap = cost
                cost = newCost
                newCost = swap
            }
            return cost[lhsLen]
        }
    }

    data class SimilarityResult(
        val percentage: Int,
        val isPassed: Boolean,
        val feedback: String
    )
}
