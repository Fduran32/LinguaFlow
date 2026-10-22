package com.linguaflow.app.data.local.dao

import androidx.room.*
import com.linguaflow.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages ORDER BY orderDisplay ASC")
    fun getAllLanguages(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM languages ORDER BY orderDisplay ASC")
    suspend fun getAllLanguagesSync(): List<LanguageEntity>

    @Query("SELECT COUNT(*) FROM languages")
    suspend fun getLanguageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<LanguageEntity>)
}

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels WHERE languageId = :languageId ORDER BY orderIndex ASC")
    fun getLevelsForLanguage(languageId: String): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE languageId = :languageId ORDER BY orderIndex ASC")
    suspend fun getLevelsForLanguageSync(languageId: String): List<LevelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevels(levels: List<LevelEntity>)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE levelId = :levelId")
    suspend fun getQuestionsForLevel(levelId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUserSync(): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setActiveUser(userId: String)

    @Transaction
    suspend fun loginUser(userId: String) {
        logoutAll()
        setActiveUser(userId)
    }

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_level_progress WHERE userId = :userId AND languageId = :languageId")
    fun getProgressForLanguage(userId: String, languageId: String): Flow<List<UserLevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(progress: UserLevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllLevelProgress(progressList: List<UserLevelProgressEntity>)

    @Query("UPDATE user_level_progress SET isCompleted = 1, isUnlocked = 1, highestScore = :score, lastCompletedAt = :timestamp WHERE userId = :userId AND levelId = :levelId")
    suspend fun markLevelCompleted(userId: String, levelId: String, score: Int, timestamp: Long)

    @Query("UPDATE user_level_progress SET isUnlocked = 1 WHERE userId = :userId AND levelId = :levelId")
    suspend fun unlockLevel(userId: String, levelId: String)

    @Query("SELECT * FROM user_language_profile WHERE userId = :userId AND languageId = :languageId")
    fun getLanguageProfile(userId: String, languageId: String): Flow<UserLanguageProfileEntity?>

    @Query("SELECT * FROM user_language_profile WHERE userId = :userId")
    fun getAllLanguageProfiles(userId: String): Flow<List<UserLanguageProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLanguageProfile(profile: UserLanguageProfileEntity)

    @Query("SELECT COUNT(*) FROM user_level_progress WHERE userId = :userId AND languageId = :languageId")
    suspend fun getUserLevelProgressCount(userId: String, languageId: String): Int
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM user_mistakes WHERE userId = :userId AND isResolved = 0 ORDER BY lastFailedAt DESC")
    fun getUnresolvedMistakes(userId: String): Flow<List<UserMistakeEntity>>

    @Query("SELECT COUNT(*) FROM user_mistakes WHERE userId = :userId AND isResolved = 0")
    fun getUnresolvedMistakesCount(userId: String): Flow<Int>

    @Query("SELECT q.* FROM questions q INNER JOIN user_mistakes m ON q.id = m.questionId WHERE m.userId = :userId AND m.isResolved = 0")
    fun getUnresolvedMistakeQuestions(userId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordMistake(mistake: UserMistakeEntity)

    @Query("UPDATE user_mistakes SET isResolved = 1 WHERE userId = :userId AND questionId = :questionId")
    suspend fun resolveMistake(userId: String, questionId: String)
}
