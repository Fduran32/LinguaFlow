package com.linguaflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val flagEmoji: String,
    val ttsLocale: String,
    val requiresPhonetic: Boolean,
    val orderDisplay: Int
)

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val id: String,
    val languageId: String,
    val orderIndex: Int,
    val title: String,
    val cefrCode: String,
    val requiredPreviousLevelId: String?
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val levelId: String,
    val type: String,
    val prompt: String,
    val promptPhonetic: String?,
    val promptAudioText: String?,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val avatar: String, // Emoji identifier (ej. "🦉", "🦊", "🦁", "🐼", "🚀", "🎓") o ruta local de archivo de imagen
    val isCustomPhoto: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
)

@Entity(
    tableName = "user_level_progress",
    primaryKeys = ["userId", "levelId"]
)
data class UserLevelProgressEntity(
    val userId: String,
    val levelId: String,
    val languageId: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val highestScore: Int,
    val lastCompletedAt: Long
)

@Entity(
    tableName = "user_mistakes",
    primaryKeys = ["userId", "questionId"]
)
data class UserMistakeEntity(
    val userId: String,
    val questionId: String,
    val lastWrongAnswer: String,
    val errorCount: Int,
    val isResolved: Boolean,
    val lastFailedAt: Long
)

@Entity(
    tableName = "user_language_profile",
    primaryKeys = ["userId", "languageId"]
)
data class UserLanguageProfileEntity(
    val userId: String,
    val languageId: String,
    val assignedCefrLevel: String, // "A1", "A2", "B1", "B2", "C1", "C2"
    val assignedLevelOrder: Int,
    val diagnosticScorePercentage: Int,
    val lastUpdated: Long
)
