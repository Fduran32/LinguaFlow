package com.linguaflow.app

import com.linguaflow.app.util.SpeechManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechManagerTest {

    @Test
    fun testExactMatchPassesWith100Score() {
        val result = SpeechManager.calculateSimilarity("Nice to meet you", "Nice to meet you")
        assertEquals(100, result.percentage)
        assertTrue(result.isPassed)
        assertTrue(result.missingWords.isEmpty())
    }

    @Test
    fun testContractionNormalization() {
        val result = SpeechManager.calculateSimilarity("I'm fine, thank you", "I am fine, thank you")
        assertTrue("Debe aprobar al usar contracciones válidas", result.isPassed)
        assertTrue("El puntaje debe ser alto con contracciones", result.percentage >= 90)
    }

    @Test
    fun testRejectsEmptySpeech() {
        val result = SpeechManager.calculateSimilarity("", "Good morning")
        assertEquals(0, result.percentage)
        assertFalse(result.isPassed)
    }

    @Test
    fun testRejectsIncompleteSpeech() {
        // El usuario solo dijo "am" cuando la frase esperada era "I am very well, thank you"
        val result = SpeechManager.calculateSimilarity("am", "I am very well, thank you")
        assertFalse("Decir solo 'am' cuando se espera una frase completa debe fallar", result.isPassed)
        assertTrue("Debe reportar palabras faltantes", result.missingWords.isNotEmpty())
    }

    @Test
    fun testCalculateBestSimilaritySelectsBestCandidate() {
        val candidates = listOf(
            "ice to meet",
            "nice to meet you",
            "night to meet"
        )
        val best = SpeechManager.calculateBestSimilarity(candidates, "Nice to meet you")
        assertTrue(best.isPassed)
        assertEquals(100, best.percentage)
    }
}
