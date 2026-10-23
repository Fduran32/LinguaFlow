package com.linguaflow.app.data.repository

import com.linguaflow.app.data.local.LinguaFlowDatabase
import com.linguaflow.app.data.local.entity.*
import com.linguaflow.app.domain.model.*
import com.linguaflow.app.domain.repository.LanguageRepository
import com.linguaflow.app.domain.repository.ProgressRepository
import com.linguaflow.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class UserRepositoryImpl(
    private val db: LinguaFlowDatabase
) : UserRepository {

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun getActiveUser(): Flow<User?> {
        return db.userDao().getActiveUser().map { entity ->
            entity?.let {
                User(
                    id = it.id,
                    username = it.username,
                    avatar = it.avatar,
                    isCustomPhoto = it.isCustomPhoto,
                    createdAt = it.createdAt
                )
            }
        }
    }

    override suspend fun getActiveUserSync(): User? {
        val entity = db.userDao().getActiveUserSync() ?: return null
        return User(
            id = entity.id,
            username = entity.username,
            avatar = entity.avatar,
            isCustomPhoto = entity.isCustomPhoto,
            createdAt = entity.createdAt
        )
    }

    override suspend fun registerUser(
        username: String,
        passwordRaw: String,
        avatar: String,
        isCustomPhoto: Boolean
    ): AuthResult {
        val cleanName = username.trim()
        val existing = db.userDao().getUserByUsername(cleanName)
        if (existing != null) {
            return AuthResult.Error("El nombre de usuario '$cleanName' ya está registrado. Por favor elige otro.")
        }

        val newId = UUID.randomUUID().toString()
        val userEntity = UserEntity(
            id = newId,
            username = cleanName,
            passwordHash = hashPassword(passwordRaw),
            avatar = avatar,
            isCustomPhoto = isCustomPhoto,
            createdAt = System.currentTimeMillis(),
            isLoggedIn = true
        )

        db.userDao().logoutAll()
        db.userDao().insertUser(userEntity)
        LinguaFlowDatabase.initializeProgressForUser(db, newId)

        return AuthResult.Success(
            User(
                id = newId,
                username = userEntity.username,
                avatar = userEntity.avatar,
                isCustomPhoto = userEntity.isCustomPhoto,
                createdAt = userEntity.createdAt
            )
        )
    }

    override suspend fun loginUser(username: String, passwordRaw: String): AuthResult {
        val cleanName = username.trim()
        val userEntity = db.userDao().getUserByUsername(cleanName)
            ?: return AuthResult.Error("Usuario no encontrado. Verifica tu nombre de usuario o regístrate.")

        if (userEntity.passwordHash != hashPassword(passwordRaw)) {
            return AuthResult.Error("Contraseña incorrecta. Por favor intenta nuevamente.")
        }

        db.userDao().loginUser(userEntity.id)

        // Asegurar que el usuario tenga su progreso inicializado
        val progressCount = db.progressDao().getUserLevelProgressCount(userEntity.id, "en")
        if (progressCount == 0) {
            LinguaFlowDatabase.initializeProgressForUser(db, userEntity.id)
        }

        return AuthResult.Success(
            User(
                id = userEntity.id,
                username = userEntity.username,
                avatar = userEntity.avatar,
                isCustomPhoto = userEntity.isCustomPhoto,
                createdAt = userEntity.createdAt
            )
        )
    }

    override suspend fun logout() {
        db.userDao().logoutAll()
    }

    override suspend fun updateProfile(
        userId: String,
        newName: String,
        newAvatar: String,
        isCustomPhoto: Boolean
    ) {
        val user = db.userDao().getUserById(userId) ?: return
        db.userDao().updateUser(
            user.copy(
                username = newName.trim(),
                avatar = newAvatar,
                isCustomPhoto = isCustomPhoto
            )
        )
    }
}

class LanguageRepositoryImpl(
    private val db: LinguaFlowDatabase
) : LanguageRepository {

    override fun getAllLanguages(): Flow<List<Language>> {
        return db.languageDao().getAllLanguages().map { entities ->
            entities.map { entity ->
                Language(
                    id = entity.id,
                    name = entity.name,
                    flagEmoji = entity.flagEmoji,
                    ttsLocale = entity.ttsLocale,
                    requiresPhonetic = entity.requiresPhonetic
                )
            }
        }
    }

    override fun getLevelsWithProgress(userId: String, languageId: String): Flow<List<Level>> {
        val levelsFlow = db.levelDao().getLevelsForLanguage(languageId)
        val progressFlow = db.progressDao().getProgressForLanguage(userId, languageId)

        return levelsFlow.combine(progressFlow) { levels, progressList ->
            val progressMap = progressList.associateBy { it.levelId }

            levels.map { levelEntity ->
                val progress = progressMap[levelEntity.id]
                Level(
                    id = levelEntity.id,
                    languageId = levelEntity.languageId,
                    orderIndex = levelEntity.orderIndex,
                    title = levelEntity.title,
                    cefrCode = levelEntity.cefrCode,
                    requiredPreviousLevelId = levelEntity.requiredPreviousLevelId,
                    isUnlocked = progress?.isUnlocked ?: (levelEntity.orderIndex == 1),
                    isCompleted = progress?.isCompleted ?: false
                )
            }
        }
    }

    override suspend fun getQuestionsForLevel(levelId: String): List<Question> {
        return db.questionDao().getQuestionsForLevel(levelId).map { entity ->
            val type = when (entity.type) {
                "FILL_BLANK" -> QuestionType.FILL_BLANK
                "TRANSLATE_BUILD" -> QuestionType.TRANSLATE_BUILD
                "LISTENING", "LISTENING_MC" -> QuestionType.LISTENING
                "SPEAKING" -> QuestionType.SPEAKING
                else -> QuestionType.MULTIPLE_CHOICE
            }
            Question(
                id = entity.id,
                lessonId = entity.levelId,
                type = type,
                prompt = entity.prompt,
                promptPhonetic = entity.promptPhonetic,
                promptAudioText = entity.promptAudioText,
                options = entity.options,
                correctAnswer = entity.correctAnswer,
                explanation = entity.explanation
            )
        }
    }
}

class ProgressRepositoryImpl(
    private val db: LinguaFlowDatabase
) : ProgressRepository {

    private fun cefrToOrder(cefr: String): Int {
        return when (cefr.take(2).uppercase()) {
            "A1" -> 1
            "A2" -> 2
            "B1" -> 3
            "B2" -> 4
            "C1" -> 5
            "C2" -> 6
            else -> 1
        }
    }

    private fun orderToCefr(order: Int): String {
        return when (order) {
            1 -> "A1"
            2 -> "A2"
            3 -> "B1"
            4 -> "B2"
            5 -> "C1"
            6 -> "C2"
            else -> "C2"
        }
    }

    override suspend fun completeLevelAndUnlockNext(
        userId: String,
        languageId: String,
        completedLevelId: String,
        score: Int
    ) {
        // Solo desbloquear si el puntaje es de al menos 80% (Opción B)
        if (score < 80) return

        val levels = db.levelDao().getLevelsForLanguage(languageId).first()
        val currentLevel = levels.firstOrNull { it.id == completedLevelId } ?: return
        val timestamp = System.currentTimeMillis()

        // 1. Marcar el subnivel actual como completado
        db.progressDao().markLevelCompleted(userId, currentLevel.id, score, timestamp)

        // 2. Buscar el siguiente subnivel dentro del mismo nivel CEFR (ej. A1.1 -> A1.2)
        val nextSubLevel = levels.firstOrNull {
            it.cefrCode == currentLevel.cefrCode && it.orderIndex == currentLevel.orderIndex + 1
        }

        if (nextSubLevel != null) {
            // Desbloquear el siguiente subnivel dentro del mismo nivel macro
            db.progressDao().unlockLevel(userId, nextSubLevel.id)
        } else {
            // Completó todos los 10 subniveles del nivel macro (ej. A1.10 completado)
            val currentMacroOrder = cefrToOrder(currentLevel.cefrCode)
            val nextMacroOrder = currentMacroOrder + 1
            val nextCefrCode = orderToCefr(nextMacroOrder)

            // Guardar el nuevo nivel macro en el perfil del usuario
            db.progressDao().saveLanguageProfile(
                UserLanguageProfileEntity(
                    userId = userId,
                    languageId = languageId,
                    assignedCefrLevel = nextCefrCode,
                    assignedLevelOrder = nextMacroOrder,
                    diagnosticScorePercentage = score,
                    lastUpdated = timestamp
                )
            )

            // Desbloquear el primer subnivel del nuevo macro nivel
            val firstSubLevelOfNext = levels.firstOrNull {
                it.cefrCode.take(2).equals(nextCefrCode.take(2), ignoreCase = true) && it.orderIndex == 1
            }
            if (firstSubLevelOfNext != null) {
                db.progressDao().unlockLevel(userId, firstSubLevelOfNext.id)
            }
        }
    }

    override suspend fun applyPlacementResult(
        userId: String,
        languageId: String,
        startingLevelOrder: Int,
        cefrCode: String,
        score: Int
    ) {
        val levels = db.levelDao().getLevelsForLanguage(languageId).first()
        val targetMacroOrder = cefrToOrder(cefrCode)

        // Guardar el perfil con el nivel asignado permanentemente en la base de datos
        db.progressDao().saveLanguageProfile(
            UserLanguageProfileEntity(
                userId = userId,
                languageId = languageId,
                assignedCefrLevel = cefrCode,
                assignedLevelOrder = targetMacroOrder,
                diagnosticScorePercentage = score,
                lastUpdated = System.currentTimeMillis()
            )
        )

        for (lvl in levels) {
            val lvlMacroOrder = cefrToOrder(lvl.cefrCode)
            when {
                lvlMacroOrder < targetMacroOrder -> {
                    // Niveles macro previos: superados y convalidados
                    db.progressDao().saveLevelProgress(
                        UserLevelProgressEntity(
                            userId = userId,
                            levelId = lvl.id,
                            languageId = languageId,
                            isUnlocked = true,
                            isCompleted = true,
                            highestScore = 100,
                            lastCompletedAt = System.currentTimeMillis()
                        )
                    )
                }
                lvlMacroOrder == targetMacroOrder -> {
                    // Nivel macro actual: Subnivel 1 desbloqueado, subniveles 2 al 10 bloqueados
                    db.progressDao().saveLevelProgress(
                        UserLevelProgressEntity(
                            userId = userId,
                            levelId = lvl.id,
                            languageId = languageId,
                            isUnlocked = lvl.orderIndex == 1,
                            isCompleted = false,
                            highestScore = 0,
                            lastCompletedAt = 0L
                        )
                    )
                }
                else -> {
                    // Niveles macro superiores: estrictamente bloqueados
                    db.progressDao().saveLevelProgress(
                        UserLevelProgressEntity(
                            userId = userId,
                            levelId = lvl.id,
                            languageId = languageId,
                            isUnlocked = false,
                            isCompleted = false,
                            highestScore = 0,
                            lastCompletedAt = 0L
                        )
                    )
                }
            }
        }
    }

    override fun getLanguageAssignedLevel(userId: String, languageId: String): Flow<String?> {
        return db.progressDao().getLanguageProfile(userId, languageId).map { it?.assignedCefrLevel }
    }

    override fun getAllLanguageProfiles(userId: String): Flow<Map<String, String>> {
        return db.progressDao().getAllLanguageProfiles(userId).map { list ->
            list.associate { it.languageId to it.assignedCefrLevel }
        }
    }

    override suspend fun recordMistake(userId: String, questionId: String, wrongAnswer: String) {
        db.mistakeDao().recordMistake(
            UserMistakeEntity(
                userId = userId,
                questionId = questionId,
                lastWrongAnswer = wrongAnswer,
                errorCount = 1,
                isResolved = false,
                lastFailedAt = System.currentTimeMillis()
            )
        )
    }

    override fun getPendingMistakesCount(userId: String): Flow<Int> {
        return db.mistakeDao().getUnresolvedMistakesCount(userId)
    }

    override fun getPendingMistakeQuestions(userId: String): Flow<List<Question>> {
        return db.mistakeDao().getUnresolvedMistakeQuestions(userId).map { entities ->
            entities.map { entity ->
                val type = when (entity.type) {
                    "FILL_BLANK" -> QuestionType.FILL_BLANK
                    "TRANSLATE_BUILD" -> QuestionType.TRANSLATE_BUILD
                    "LISTENING", "LISTENING_MC" -> QuestionType.LISTENING
                    "SPEAKING" -> QuestionType.SPEAKING
                    else -> QuestionType.MULTIPLE_CHOICE
                }
                Question(
                    id = entity.id,
                    lessonId = entity.levelId,
                    type = type,
                    prompt = entity.prompt,
                    promptPhonetic = entity.promptPhonetic,
                    promptAudioText = entity.promptAudioText,
                    options = entity.options,
                    correctAnswer = entity.correctAnswer,
                    explanation = entity.explanation
                )
            }
        }
    }

    override suspend fun resolveMistake(userId: String, questionId: String) {
        db.mistakeDao().resolveMistake(userId, questionId)
    }
}
