package com.linguaflow.app.ui.screens.path

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.linguaflow.app.data.local.LinguaFlowDatabase
import com.linguaflow.app.data.repository.LanguageRepositoryImpl
import com.linguaflow.app.data.repository.ProgressRepositoryImpl
import com.linguaflow.app.data.repository.UserRepositoryImpl
import com.linguaflow.app.domain.model.AuthResult
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.Level
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LearningPathUiState(
    val currentUser: User? = null,
    val languages: List<Language> = emptyList(),
    val currentLanguage: Language = Language("en", "Inglés", "🇺🇸", "en-US", false),
    val levels: List<Level> = emptyList(),
    val userAssignedLevel: String = "A1",
    val selectedViewingLevel: String = "A1",
    val availablePracticeLevels: List<String> = listOf("A1"),
    val allLanguageLevels: Map<String, String> = emptyMap(),
    val pendingMistakesCount: Int = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class LearningPathViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LinguaFlowDatabase.getDatabase(application, viewModelScope)
    private val languageRepo = LanguageRepositoryImpl(db)
    private val progressRepo = ProgressRepositoryImpl(db)
    private val userRepo = UserRepositoryImpl(db)

    private val _currentLanguageId = MutableStateFlow("en")
    private val _selectedViewingLevel = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(LearningPathUiState())
    val uiState: StateFlow<LearningPathUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            LinguaFlowDatabase.ensureSeeded(application, db)
            observeData()
        }
    }

    private fun observeData() {
        val allCefrStages = listOf("A1", "A2", "B1", "B2", "C1", "C2")

        viewModelScope.launch {
            userRepo.getActiveUser().flatMapLatest { activeUser ->
                if (activeUser == null) {
                    flowOf(LearningPathUiState(currentUser = null, isLoading = false))
                } else {
                    combine(
                        languageRepo.getAllLanguages(),
                        _currentLanguageId,
                        progressRepo.getPendingMistakesCount(activeUser.id),
                        progressRepo.getAllLanguageProfiles(activeUser.id)
                    ) { languages, activeLangId, mistakesCount, languageProfiles ->
                        val active = languages.firstOrNull { it.id == activeLangId }
                            ?: Language("en", "Inglés", "🇺🇸", "en-US", false)
                        DataBundle(languages, active, mistakesCount, languageProfiles)
                    }.flatMapLatest { bundle ->
                        combine(
                            languageRepo.getLevelsWithProgress(activeUser.id, bundle.activeLanguage.id),
                            progressRepo.getLanguageAssignedLevel(activeUser.id, bundle.activeLanguage.id),
                            _selectedViewingLevel
                        ) { levels, assignedLevel, customViewing ->
                            val activeCefr = assignedLevel ?: "A1"
                            val maxOrderIdx = allCefrStages.indexOf(activeCefr.take(2).uppercase()).coerceAtLeast(0)
                            val availablePractice = allCefrStages.take(maxOrderIdx + 1)

                            val viewingCefr = if (customViewing != null && availablePractice.contains(customViewing)) {
                                customViewing
                            } else {
                                activeCefr
                            }

                            // Filtrar en la ruta exclusivamente los subniveles del nivel seleccionado para ver/practicar
                            val activeSubLevels = levels.filter {
                                it.cefrCode.take(2).equals(viewingCefr.take(2), ignoreCase = true)
                            }

                            LearningPathUiState(
                                currentUser = activeUser,
                                languages = bundle.languages,
                                currentLanguage = bundle.activeLanguage,
                                levels = activeSubLevels,
                                userAssignedLevel = activeCefr,
                                selectedViewingLevel = viewingCefr,
                                availablePracticeLevels = availablePractice,
                                allLanguageLevels = bundle.languageProfiles,
                                pendingMistakesCount = bundle.mistakesCount,
                                isLoading = false
                            )
                        }
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private data class DataBundle(
        val languages: List<Language>,
        val activeLanguage: Language,
        val mistakesCount: Int,
        val languageProfiles: Map<String, String>
    )

    fun switchLanguage(languageId: String) {
        _selectedViewingLevel.value = null
        _currentLanguageId.value = languageId
    }

    fun selectViewingLevel(cefr: String) {
        _selectedViewingLevel.value = cefr
    }

    suspend fun registerUser(
        username: String,
        passwordRaw: String,
        avatar: String,
        isCustomPhoto: Boolean
    ): AuthResult {
        return userRepo.registerUser(username, passwordRaw, avatar, isCustomPhoto)
    }

    suspend fun loginUser(username: String, passwordRaw: String): AuthResult {
        return userRepo.loginUser(username, passwordRaw)
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.logout()
            _selectedViewingLevel.value = null
        }
    }

    fun updateProfile(newName: String, newAvatar: String, isCustomPhoto: Boolean) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.updateProfile(user.id, newName, newAvatar, isCustomPhoto)
        }
    }

    suspend fun getQuestionsForLevel(levelId: String): List<Question> {
        return languageRepo.getQuestionsForLevel(levelId)
    }

    suspend fun getPracticeQuestions(limit: Int = 10): List<Question> {
        val activeLangId = _currentLanguageId.value
        val currentCefr = _uiState.value.userAssignedLevel.take(2).uppercase()
        val allLevels = db.levelDao().getLevelsForLanguageSync(activeLangId)
        val matchingLevels = allLevels.filter { it.cefrCode.take(2).equals(currentCefr, ignoreCase = true) }
        val targetLevels = if (matchingLevels.isNotEmpty()) matchingLevels else allLevels.take(3)
        val questions = mutableListOf<Question>()
        for (lvl in targetLevels) {
            val qEntities = db.questionDao().getQuestionsForLevel(lvl.id)
            for (q in qEntities) {
                questions.add(
                    Question(
                        id = q.id,
                        lessonId = q.levelId,
                        type = com.linguaflow.app.domain.model.QuestionType.valueOf(q.type),
                        prompt = q.prompt,
                        promptPhonetic = q.promptPhonetic,
                        promptAudioText = q.promptAudioText,
                        options = q.options,
                        correctAnswer = q.correctAnswer,
                        explanation = q.explanation
                    )
                )
            }
        }
        return questions.shuffled().take(limit)
    }

    suspend fun getPendingMistakeQuestions(): List<Question> {
        val userId = _uiState.value.currentUser?.id ?: return emptyList()
        return progressRepo.getPendingMistakeQuestions(userId).first()
    }

    fun onLevelCompleted(completedLevelId: String, score: Int) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            progressRepo.completeLevelAndUnlockNext(
                userId = userId,
                languageId = _currentLanguageId.value,
                completedLevelId = completedLevelId,
                score = score
            )
        }
    }

    fun onPlacementTestFinished(startingLevelOrder: Int, cefrCode: String, score: Int) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            progressRepo.applyPlacementResult(
                userId = userId,
                languageId = _currentLanguageId.value,
                startingLevelOrder = startingLevelOrder,
                cefrCode = cefrCode,
                score = score
            )
        }
    }

    fun recordMistake(questionId: String, wrongAnswer: String) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            progressRepo.recordMistake(userId, questionId, wrongAnswer)
        }
    }

    fun resolveMistake(questionId: String) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            progressRepo.resolveMistake(userId, questionId)
        }
    }
}
