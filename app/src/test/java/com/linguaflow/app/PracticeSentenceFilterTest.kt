package com.linguaflow.app

import com.linguaflow.app.domain.util.PracticeSentenceFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSentenceFilterTest {

    @Test
    fun testRejectsSingleWords() {
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("am"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("is"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("where"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("dog"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("water"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence(""))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("   "))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence(null))
    }

    @Test
    fun testRejectsFillInBlanksAndPlaceholders() {
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("I ___ a doctor"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("She is ___ sister"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("Hi, ___ to meet you!"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("Where is the ...?"))
    }

    @Test
    fun testRejectsSpanishMetaQuestions() {
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("¿Cómo se saluda cortésmente en la mañana en inglés?"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("¿Cuál es la forma correcta de 'to be'?"))
    }

    @Test
    fun testRejectsShortFragments() {
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("I am"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("at in"))
        assertFalse(PracticeSentenceFilter.isMeaningfulSentence("to go"))
    }

    @Test
    fun testAcceptsCompleteMeaningfulSentences() {
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("Nice to meet you"))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("Good morning"))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("I drink water"))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("She is reading a book"))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("Where is the nearest supermarket?"))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("They have two dogs and a cat."))
        assertTrue(PracticeSentenceFilter.isMeaningfulSentence("I am very well, thank you"))
    }

    @Test
    fun testCleanSpanishPromptText() {
        assertEquals(
            "Gusto en conocerte",
            PracticeSentenceFilter.cleanSpanishPromptText("Ordena las fichas: 'Gusto en conocerte'")
        )
        assertEquals(
            "Hola, mi nombre es Alex",
            PracticeSentenceFilter.cleanSpanishPromptText("Pronuncia en voz alta: 'Hola, mi nombre es Alex'")
        )
        assertEquals(
            "Ella es mi mejor amiga",
            PracticeSentenceFilter.cleanSpanishPromptText("Traduce la siguiente frase al inglés: 'Ella es mi mejor amiga'")
        )
        assertEquals(
            "Buenos días a todos",
            PracticeSentenceFilter.cleanSpanishPromptText("Traduce: 'Buenos días a todos'")
        )
    }
}
