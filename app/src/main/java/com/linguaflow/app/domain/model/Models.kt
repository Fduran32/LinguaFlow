package com.linguaflow.app.domain.model

data class Language(
    val id: String,
    val name: String,
    val flagEmoji: String,
    val ttsLocale: String,
    val requiresPhonetic: Boolean
)

data class User(
    val id: String,
    val username: String,
    val avatar: String,
    val isCustomPhoto: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class Level(
    val id: String,
    val languageId: String,
    val orderIndex: Int,
    val title: String,
    val cefrCode: String,
    val requiredPreviousLevelId: String?,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)

data class Lesson(
    val id: String,
    val levelId: String,
    val orderIndex: Int,
    val title: String,
    val xpReward: Int = 10,
    val isCompleted: Boolean = false,
    val highestScore: Int = 0
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    FILL_BLANK,
    TRANSLATE_BUILD,
    LISTENING,
    LISTENING_MC,
    SPEAKING
}

data class Question(
    val id: String,
    val lessonId: String,
    val type: QuestionType,
    val prompt: String,
    val promptPhonetic: String? = null,
    val promptAudioText: String? = null,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class AnswerResult(
    val isCorrect: Boolean,
    val selectedAnswer: String,
    val correctAnswer: String,
    val explanation: String
)

data class LevelAccessStatus(
    val isUnlocked: Boolean,
    val messageIfNot: String? = null
)
