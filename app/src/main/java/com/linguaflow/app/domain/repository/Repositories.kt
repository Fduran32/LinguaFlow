package com.linguaflow.app.domain.repository

import com.linguaflow.app.domain.model.AuthResult
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.Level
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getActiveUser(): Flow<User?>
    suspend fun getActiveUserSync(): User?
    suspend fun registerUser(username: String, passwordRaw: String, avatar: String, isCustomPhoto: Boolean): AuthResult
    suspend fun loginUser(username: String, passwordRaw: String): AuthResult
    suspend fun logout()
    suspend fun updateProfile(userId: String, newName: String, newAvatar: String, isCustomPhoto: Boolean)
}

interface LanguageRepository {
    fun getAllLanguages(): Flow<List<Language>>
    fun getLevelsWithProgress(userId: String, languageId: String): Flow<List<Level>>
    suspend fun getQuestionsForLevel(levelId: String): List<Question>
}

interface ProgressRepository {
    suspend fun completeLevelAndUnlockNext(userId: String, languageId: String, completedLevelId: String, score: Int)
    suspend fun applyPlacementResult(userId: String, languageId: String, startingLevelOrder: Int, cefrCode: String, score: Int)
    fun getLanguageAssignedLevel(userId: String, languageId: String): Flow<String?>
    fun getAllLanguageProfiles(userId: String): Flow<Map<String, String>>
    suspend fun recordMistake(userId: String, questionId: String, wrongAnswer: String)
    fun getPendingMistakeQuestions(userId: String): Flow<List<Question>>
    fun getPendingMistakesCount(userId: String): Flow<Int>
    suspend fun resolveMistake(userId: String, questionId: String)
}
