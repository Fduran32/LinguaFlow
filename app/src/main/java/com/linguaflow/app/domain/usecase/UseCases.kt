package com.linguaflow.app.domain.usecase

import com.linguaflow.app.domain.model.AnswerResult
import com.linguaflow.app.domain.model.LevelAccessStatus
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.QuestionType
import com.linguaflow.app.util.SpeechManager

/**
 * Caso de Uso: Evaluar respuesta con retroalimentación inmediata y explicación.
 * Cumple con RF-10, RF-11, RF-12.
 * Soporta normalización completa de contracciones ("she's" == "she is", "I'm" == "I am", apóstrofos curvos)
 * y evaluación fonética tolerante en ejercicios de SPEAKING.
 */
class EvaluateAnswerUseCase {
    fun execute(question: Question, selectedAnswer: String): AnswerResult {
        if (question.type == QuestionType.SPEAKING) {
            val similarity = SpeechManager.calculateSimilarity(selectedAnswer, question.correctAnswer)
            return AnswerResult(
                isCorrect = similarity.isPassed,
                selectedAnswer = selectedAnswer,
                correctAnswer = question.correctAnswer,
                explanation = if (similarity.isPassed) {
                    question.explanation
                } else {
                    "${similarity.feedback} (${similarity.percentage}%). ${question.explanation}"
                }
            )
        }

        val cleanUser = SpeechManager.cleanText(selectedAnswer)
        val cleanCorrect = SpeechManager.cleanText(question.correctAnswer)

        val isCorrect = cleanUser.isNotBlank() && (
            cleanUser == cleanCorrect ||
                cleanUser.replace("\\s+".toRegex(), "") == cleanCorrect.replace("\\s+".toRegex(), "")
        )

        return AnswerResult(
            isCorrect = isCorrect,
            selectedAnswer = selectedAnswer,
            correctAnswer = question.correctAnswer,
            explanation = question.explanation
        )
    }
}

/**
 * Caso de Uso: Validar si un nivel está bloqueado o desbloqueado.
 * Cumple estrictamente con la Regla de Negocio RN-01.
 */
class UnlockLevelUseCase {
    fun execute(
        levelOrder: Int,
        previousLevelCompleted: Boolean
    ): LevelAccessStatus {
        // El nivel 1 siempre está disponible
        if (levelOrder <= 1) {
            return LevelAccessStatus(isUnlocked = true)
        }

        // Si el nivel anterior no está superado con >= 80%, se bloquea
        return if (previousLevelCompleted) {
            LevelAccessStatus(isUnlocked = true)
        } else {
            LevelAccessStatus(
                isUnlocked = false,
                messageIfNot = "🔒 Este nivel está bloqueado. Debes completar primero el Nivel anterior con al menos un 80% de aciertos."
            )
        }
    }
}

/**
 * Caso de Uso: Asignación de nivel por prueba de diagnóstico inicial.
 * Cumple con RN-02 y RF-04.
 */
class ProcessPlacementTestUseCase {
    data class PlacementResult(
        val startingLevelOrder: Int,
        val levelName: String,
        val message: String
    )

    fun execute(scorePercentage: Int): PlacementResult {
        return when {
            scorePercentage <= 35 -> PlacementResult(
                startingLevelOrder = 1,
                levelName = "Nivel 1 (Principiante A1)",
                message = "Empezarás desde el Nivel 1 para construir bases sólidas."
            )
            scorePercentage <= 65 -> PlacementResult(
                startingLevelOrder = 2,
                levelName = "Nivel 2 (Básico A2)",
                message = "¡Buen trabajo! Has desbloqueado el Nivel 1 e inicias en el Nivel 2."
            )
            else -> PlacementResult(
                startingLevelOrder = 3,
                levelName = "Nivel 3 (Intermedio B1)",
                message = "¡Excelente dominio! Has convalidado los Niveles 1 y 2, comienzas en el Nivel 3."
            )
        }
    }
}
