package com.linguaflow.app

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linguaflow.app.domain.model.QuestionType
import com.linguaflow.app.domain.util.PracticeSentenceFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeedContentIntegrityTest {

    @Test
    fun testSeedContentIntegrityAndValidTypes() {
        val assetFile = File("src/main/assets/seed_content.json")
        assertTrue("El archivo seed_content.json debe existir en assets", assetFile.exists())

        val jsonString = assetFile.readText()
        val root = Gson().fromJson(jsonString, JsonObject::class.java)
        val languages = root.getAsJsonArray("languages")
        assertNotNull(languages)
        assertTrue("Debe existir al menos un idioma", languages.size() > 0)

        val validEnumTypes = QuestionType.values().map { it.name }.toSet()

        var totalQuestions = 0
        var completeSentencesCount = 0

        for (langElement in languages) {
            val langObj = langElement.asJsonObject
            val langId = langObj.get("id").asString
            assertFalse("El ID del idioma no debe estar en blanco", langId.isBlank())
            val levels = langObj.getAsJsonArray("levels")

            for (lvlElement in levels) {
                val lvlObj = lvlElement.asJsonObject
                val lessons = lvlObj.getAsJsonArray("lessons")

                for (lesElement in lessons) {
                    val lesObj = lesElement.asJsonObject
                    val questions = lesObj.getAsJsonArray("questions")

                    for (qElement in questions) {
                        val qObj = qElement.asJsonObject
                        totalQuestions++

                        val qId = qObj.get("id").asString
                        val qType = qObj.get("type").asString
                        val prompt = qObj.get("prompt").asString
                        val correctAnswer = qObj.get("correct_answer").asString

                        assertFalse("El ID de la pregunta no debe estar en blanco ($qId)", qId.isBlank())
                        assertFalse("El prompt de la pregunta no debe estar en blanco ($qId)", prompt.isBlank())
                        assertFalse("La respuesta correcta no debe estar en blanco ($qId)", correctAnswer.isBlank())

                        // Verificación crítica: el tipo debe ser exactamente uno de los valores válidos del enum
                        assertTrue(
                            "El tipo de pregunta '$qType' en '$qId' debe ser un QuestionType válido en Models.kt",
                            validEnumTypes.contains(qType)
                        )

                        if (PracticeSentenceFilter.isMeaningfulSentence(correctAnswer)) {
                            completeSentencesCount++
                        }
                    }
                }
            }
        }

        assertTrue("Deben existir al menos 100 preguntas en total (actual: $totalQuestions)", totalQuestions >= 100)
        assertTrue(
            "Deben existir al menos 300 oraciones completas y significativas para el Centro de Entrenamiento (actual: $completeSentencesCount)",
            completeSentencesCount >= 300
        )
    }
}
