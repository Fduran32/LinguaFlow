package com.linguaflow.app.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linguaflow.app.data.local.dao.*
import com.linguaflow.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

@Database(
    entities = [
        UserEntity::class,
        LanguageEntity::class,
        LevelEntity::class,
        QuestionEntity::class,
        UserLevelProgressEntity::class,
        UserMistakeEntity::class,
        UserLanguageProfileEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LinguaFlowDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun languageDao(): LanguageDao
    abstract fun levelDao(): LevelDao
    abstract fun questionDao(): QuestionDao
    abstract fun progressDao(): ProgressDao
    abstract fun mistakeDao(): MistakeDao

    companion object {
        @Volatile
        private var INSTANCE: LinguaFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LinguaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinguaFlowDatabase::class.java,
                    "linguaflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            scope.launch(Dispatchers.IO) {
                                INSTANCE?.let { ensureSeeded(context.applicationContext, it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureSeeded(context: Context, db: LinguaFlowDatabase) {
            withContext(Dispatchers.IO) {
                try {
                    val count = db.languageDao().getLanguageCount()
                    if (count == 0) {
                        populateInitialData(context, db)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private suspend fun populateInitialData(context: Context, db: LinguaFlowDatabase) {
            try {
                val assetManager = context.assets
                val inputStream = assetManager.open("seed_content.json")
                val reader = InputStreamReader(inputStream)
                val jsonObject = Gson().fromJson(reader, JsonObject::class.java)

                val languagesArray = jsonObject.getAsJsonArray("languages")

                val languageEntities = mutableListOf<LanguageEntity>()
                val levelEntities = mutableListOf<LevelEntity>()
                val questionEntities = mutableListOf<QuestionEntity>()
                val initialProgress = mutableListOf<UserLevelProgressEntity>()

                var langIndex = 0
                for (langElem in languagesArray) {
                    val langObj = langElem.asJsonObject
                    val langId = langObj.get("id").asString
                    val langName = langObj.get("name").asString
                    val flag = langObj.get("flag_emoji").asString
                    val locale = langObj.get("tts_locale").asString
                    val phonetic = langObj.get("requires_phonetic").asBoolean

                    languageEntities.add(
                        LanguageEntity(langId, langName, flag, locale, phonetic, langIndex++)
                    )

                    val levelsArray = langObj.getAsJsonArray("levels")
                    for (lvlElem in levelsArray) {
                        val lvlObj = lvlElem.asJsonObject
                        val lvlId = lvlObj.get("id").asString
                        val orderIndex = lvlObj.get("order_index").asInt
                        val title = lvlObj.get("title").asString
                        val cefr = lvlObj.get("cefr_code").asString
                        val prev = if (lvlObj.get("required_previous_level_id").isJsonNull) null else lvlObj.get("required_previous_level_id").asString

                        levelEntities.add(
                            LevelEntity(lvlId, langId, orderIndex, title, cefr, prev)
                        )

                        val lessonsArray = lvlObj.getAsJsonArray("lessons")
                        for (lesElem in lessonsArray) {
                            val lesObj = lesElem.asJsonObject
                            val questionsArray = lesObj.getAsJsonArray("questions")
                            for (qElem in questionsArray) {
                                val qObj = qElem.asJsonObject
                                val qId = qObj.get("id").asString
                                val qType = qObj.get("type").asString
                                val prompt = qObj.get("prompt").asString
                                val pPhonetic = if (qObj.has("prompt_phonetic") && !qObj.get("prompt_phonetic").isJsonNull) qObj.get("prompt_phonetic").asString else null
                                val pAudio = if (qObj.has("prompt_audio_text") && !qObj.get("prompt_audio_text").isJsonNull) qObj.get("prompt_audio_text").asString else null
                                val options = Gson().fromJson(qObj.getAsJsonArray("options"), Array<String>::class.java).toList()
                                val correct = qObj.get("correct_answer").asString
                                val expl = qObj.get("explanation").asString

                                questionEntities.add(
                                    QuestionEntity(
                                        id = qId,
                                        levelId = lvlId,
                                        type = qType,
                                        prompt = prompt,
                                        promptPhonetic = pPhonetic,
                                        promptAudioText = pAudio,
                                        options = options,
                                        correctAnswer = correct,
                                        explanation = expl
                                    )
                                )
                            }
                        }
                    }
                }

                db.languageDao().insertLanguages(languageEntities)
                db.levelDao().insertLevels(levelEntities)
                db.questionDao().insertQuestions(questionEntities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        suspend fun initializeProgressForUser(db: LinguaFlowDatabase, userId: String) {
            withContext(Dispatchers.IO) {
                val languages = db.languageDao().getAllLanguagesSync()
                val initialProgress = mutableListOf<UserLevelProgressEntity>()
                val timestamp = System.currentTimeMillis()

                for (lang in languages) {
                    db.progressDao().saveLanguageProfile(
                        UserLanguageProfileEntity(
                            userId = userId,
                            languageId = lang.id,
                            assignedCefrLevel = "A1",
                            assignedLevelOrder = 1,
                            diagnosticScorePercentage = 0,
                            lastUpdated = timestamp
                        )
                    )

                    val levels = db.levelDao().getLevelsForLanguageSync(lang.id)
                    for (lvl in levels) {
                        initialProgress.add(
                            UserLevelProgressEntity(
                                userId = userId,
                                levelId = lvl.id,
                                languageId = lang.id,
                                isUnlocked = lvl.cefrCode.take(2).uppercase() == "A1" && lvl.orderIndex == 1,
                                isCompleted = false,
                                highestScore = 0,
                                lastCompletedAt = 0L
                            )
                        )
                    }
                }

                db.progressDao().saveAllLevelProgress(initialProgress)
            }
        }
    }
}
