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
        onFinalResult: (bestResult: String, allCandidates: List<String>) -> Unit,
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
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se detectó la frase. Intenta pronunciar más cerca del micrófono."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó audio a tiempo. Toca y habla de inmediato."
                            SpeechRecognizer.ERROR_AUDIO -> "Error de grabación de audio."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Se requiere permiso de micrófono."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Verifica tu conexión para reconocimiento de voz."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Micrófono ocupado. Intenta de nuevo."
                            else -> "Intenta pronunciar nuevamente."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf<String>()
                        val primary = matches.firstOrNull() ?: ""
                        onFinalResult(primary, matches)
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
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5) // Hasta 5 hipótesis para máxima comprensión
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
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
         * Evalúa múltiples candidatos devueltos por Google Speech y selecciona
         * el que mejor coincida con la frase esperada, dando flexibilidad de acento.
         */
        fun calculateBestSimilarity(candidates: List<String>, target: String): SimilarityResult {
            if (candidates.isEmpty()) {
                return calculateSimilarity("", target)
            }
            var bestResult = calculateSimilarity(candidates.first(), target)
            for (cand in candidates.drop(1)) {
                val eval = calculateSimilarity(cand, target)
                if (eval.percentage > bestResult.percentage) {
                    bestResult = eval
                }
            }
            return bestResult
        }

        /**
         * Compara el texto hablado con la frase objetivo con normalización de contracciones
         * y tolerancia para hablantes hispanos en aprendizaje.
         */
        fun calculateSimilarity(spoken: String, target: String): SimilarityResult {
            val cleanSpoken = cleanText(spoken)
            val cleanTarget = cleanText(target)

            if (cleanSpoken.isEmpty()) {
                return SimilarityResult(
                    percentage = 0,
                    isPassed = false,
                    feedback = "No se escuchó ninguna palabra. Intenta hablar más cerca.",
                    bestSpoken = spoken
                )
            }

            if (cleanSpoken == cleanTarget) {
                return SimilarityResult(
                    percentage = 100,
                    isPassed = true,
                    feedback = "¡Pronunciación excelente! 🌟",
                    bestSpoken = spoken,
                    matchedWords = cleanTarget.split(" ").filter { it.isNotBlank() }
                )
            }

            // Comparación de palabras clave
            val targetWords = cleanTarget.split(" ").filter { it.isNotBlank() }
            val spokenWords = cleanSpoken.split(" ").filter { it.isNotBlank() }

            val matchedWords = mutableListOf<String>()
            val missingWords = mutableListOf<String>()

            for (targetWord in targetWords) {
                if (spokenWords.any { it.equals(targetWord, ignoreCase = true) || levenshtein(it, targetWord) <= 1 }) {
                    matchedWords.add(targetWord)
                } else {
                    missingWords.add(targetWord)
                }
            }

            val wordScore = if (targetWords.isNotEmpty()) (matchedWords.size * 100) / targetWords.size else 0
            val levDistance = levenshtein(cleanSpoken, cleanTarget)
            val maxLen = max(cleanSpoken.length, cleanTarget.length)
            val levScore = if (maxLen > 0) ((maxLen - levDistance) * 100) / maxLen else 0

            val finalScore = ((wordScore * 0.7) + (levScore * 0.3)).toInt().coerceIn(0, 100)
            val wordsRatio = if (targetWords.isNotEmpty()) matchedWords.size.toFloat() / targetWords.size.toFloat() else 0f
            
            // Rigor de completitud: para aprobar debe pronunciar al menos el 80% de las palabras y score >= 75.
            // En frases cortas (<= 3 palabras), no puede faltar ninguna palabra.
            val isPassed = if (targetWords.size <= 3) {
                missingWords.isEmpty() && finalScore >= 75
            } else {
                wordsRatio >= 0.8f && finalScore >= 75
            }

            val feedback = when {
                finalScore >= 90 && missingWords.isEmpty() -> "¡Excelente pronunciación! 🌟"
                isPassed -> "¡Muy bien! Se entendió casi a la perfección 👍"
                missingWords.isNotEmpty() -> "Incompleto. Te faltó pronunciar: \"${missingWords.joinToString(" ")}\""
                else -> "Pronunciación poco clara ($finalScore%). Intenta pronunciar más claro y pausado."
            }

            return SimilarityResult(
                percentage = finalScore,
                isPassed = isPassed,
                feedback = feedback,
                bestSpoken = spoken,
                matchedWords = matchedWords,
                missingWords = missingWords
            )
        }

        private fun cleanText(text: String): String {
            return normalizeContractions(text)
                .replace(Regex("[.,?!;:'\"¿¡-]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun normalizeContractions(text: String): String {
            return text.lowercase(Locale.ROOT)
                .replace("she's", "she is")
                .replace("he's", "he is")
                .replace("it's", "it is")
                .replace("i'm", "i am")
                .replace("you're", "you are")
                .replace("we're", "we are")
                .replace("they're", "they are")
                .replace("that's", "that is")
                .replace("there's", "there is")
                .replace("who's", "who is")
                .replace("what's", "what is")
                .replace("where's", "where is")
                .replace("don't", "do not")
                .replace("doesn't", "does not")
                .replace("didn't", "did not")
                .replace("can't", "cannot")
                .replace("couldn't", "could not")
                .replace("won't", "will not")
                .replace("wouldn't", "would not")
                .replace("isn't", "is not")
                .replace("aren't", "are not")
                .replace("wasn't", "was not")
                .replace("weren't", "were not")
                .replace("haven't", "have not")
                .replace("hasn't", "has not")
                .replace("hadn't", "had not")
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
        val feedback: String,
        val bestSpoken: String = "",
        val matchedWords: List<String> = emptyList(),
        val missingWords: List<String> = emptyList()
    )
}
