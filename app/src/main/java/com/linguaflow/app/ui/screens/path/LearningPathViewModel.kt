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
import com.linguaflow.app.ui.screens.practice.PracticeSkillMode
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

    private fun isMeaningfulSentence(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val clean = text.trim()
        // Descartar placeholders de rellenar huecos
        if (clean.contains("___") || clean.contains("...") || clean.contains("_")) return false
        // Descartar si empieza con signos de interrogación invertidos (preguntas en español tipo "¿Cómo se dice...?")
        if (clean.startsWith("¿") || clean.startsWith("?")) return false
        // Contar palabras
        val words = clean.split(Regex("[\\s,;:.!?¿¡\"']+")).filter { it.isNotBlank() }
        // Mínimo 2 palabras y longitud al menos 8 caracteres (para descartar monosílabos o pares cortos como "I am", "to go", "at in")
        // O si tiene 3 o más palabras (ej: "I am fine")
        if (words.size < 2) return false
        if (words.size < 3 && clean.length < 8) return false
        return true
    }

    private fun cleanSpanishPromptText(prompt: String): String {
        return prompt
            .replace(Regex("^(Ordena( las fichas)?|Traduce( la siguiente frase)?( al inglés)?|Pronuncia( en voz alta)?):\\s*['\"]?", RegexOption.IGNORE_CASE), "")
            .replace(Regex("['\"]$"), "")
            .trim()
    }

    suspend fun getPracticeQuestions(skillMode: PracticeSkillMode = PracticeSkillMode.MIX, limit: Int = 10): List<Question> {
        val activeLangId = _currentLanguageId.value
        val currentCefr = _uiState.value.userAssignedLevel.take(2).uppercase()
        val allLevels = db.levelDao().getLevelsForLanguageSync(activeLangId)
        val matchingLevels = allLevels.filter { it.cefrCode.take(2).equals(currentCefr, ignoreCase = true) }
        val targetLevels = if (matchingLevels.isNotEmpty()) matchingLevels else allLevels

        val candidateQuestions = mutableListOf<Question>()

        for (lvl in targetLevels) {
            val qEntities = db.questionDao().getQuestionsForLevel(lvl.id)
            for (q in qEntities) {
                val qType = when (q.type) {
                    "FILL_BLANK" -> com.linguaflow.app.domain.model.QuestionType.FILL_BLANK
                    "TRANSLATE_BUILD" -> com.linguaflow.app.domain.model.QuestionType.TRANSLATE_BUILD
                    "LISTENING", "LISTENING_MC" -> com.linguaflow.app.domain.model.QuestionType.LISTENING
                    "SPEAKING" -> com.linguaflow.app.domain.model.QuestionType.SPEAKING
                    else -> com.linguaflow.app.domain.model.QuestionType.MULTIPLE_CHOICE
                }

                val answerEnglish = q.correctAnswer.trim()
                val audioEnglish = q.promptAudioText?.trim()
                val phrase = if (isMeaningfulSentence(audioEnglish)) {
                    audioEnglish!!
                } else if (isMeaningfulSentence(answerEnglish)) {
                    answerEnglish
                } else {
                    ""
                }

                // Filtrar absolutamente cualquier fragmento corto o monosílabo ("am", "is", "where", etc.)
                if (!isMeaningfulSentence(phrase)) continue

                when (skillMode) {
                    PracticeSkillMode.WRITING -> {
                        // Modo Writing: traducción directa al inglés de oraciones completas
                        if (!q.prompt.contains("___") && !q.prompt.startsWith("¿") && !q.prompt.startsWith("?")) {
                            val cleanSpanish = cleanSpanishPromptText(q.prompt)
                            if (cleanSpanish.isNotBlank() && cleanSpanish != phrase) {
                                candidateQuestions.add(
                                    Question(
                                        id = q.id,
                                        lessonId = q.levelId,
                                        type = com.linguaflow.app.domain.model.QuestionType.FILL_BLANK,
                                        prompt = cleanSpanish,
                                        promptPhonetic = null,
                                        promptAudioText = null,
                                        options = emptyList(),
                                        correctAnswer = phrase,
                                        explanation = q.explanation
                                    )
                                )
                            }
                        }
                    }

                    PracticeSkillMode.SPEAKING -> {
                        // Modo Speaking: frases completas y comunicativas para pronunciar
                        if (qType == com.linguaflow.app.domain.model.QuestionType.SPEAKING ||
                            qType == com.linguaflow.app.domain.model.QuestionType.TRANSLATE_BUILD ||
                            qType == com.linguaflow.app.domain.model.QuestionType.LISTENING
                        ) {
                            candidateQuestions.add(
                                Question(
                                    id = q.id,
                                    lessonId = q.levelId,
                                    type = com.linguaflow.app.domain.model.QuestionType.SPEAKING,
                                    prompt = "Pronuncia en voz alta en inglés:",
                                    promptPhonetic = q.promptPhonetic,
                                    promptAudioText = phrase,
                                    options = emptyList(),
                                    correctAnswer = phrase,
                                    explanation = q.explanation
                                )
                            )
                        }
                    }

                    PracticeSkillMode.LISTENING -> {
                        // Modo Listening: audios de oraciones reales para entrenar el oído
                        candidateQuestions.add(
                            Question(
                                id = q.id,
                                lessonId = q.levelId,
                                type = com.linguaflow.app.domain.model.QuestionType.LISTENING,
                                prompt = "Escucha atentamente el audio y escribe lo que oyes:",
                                promptPhonetic = null,
                                promptAudioText = phrase,
                                options = emptyList(),
                                correctAnswer = phrase,
                                explanation = q.explanation
                            )
                        )
                    }

                    PracticeSkillMode.MIX -> {
                        // En MIX, acumulamos candidatos válidos con significado completo
                        val cleanSpanish = cleanSpanishPromptText(q.prompt)
                        val spanishValid = !q.prompt.contains("___") && !q.prompt.startsWith("¿") && cleanSpanish.isNotBlank() && cleanSpanish != phrase
                        candidateQuestions.add(
                            Question(
                                id = q.id,
                                lessonId = q.levelId,
                                type = qType,
                                prompt = if (spanishValid) cleanSpanish else "Pronuncia o escucha la siguiente frase en inglés:",
                                promptPhonetic = q.promptPhonetic,
                                promptAudioText = phrase,
                                options = emptyList(),
                                correctAnswer = phrase,
                                explanation = q.explanation
                            )
                        )
                    }
                }
            }
        }

        if (skillMode == PracticeSkillMode.MIX) {
            // Distribuir equitativamente entre Speaking (0), Listening (1) y Writing (2)
            val shuffled = candidateQuestions.shuffled().take(limit)
            return shuffled.mapIndexed { index, q ->
                when (index % 3) {
                    0 -> q.copy(
                        type = com.linguaflow.app.domain.model.QuestionType.SPEAKING,
                        prompt = "Pronuncia en voz alta en inglés:"
                    )
                    1 -> q.copy(
                        type = com.linguaflow.app.domain.model.QuestionType.LISTENING,
                        prompt = "Escucha atentamente el audio y escribe lo que oyes:"
                    )
                    else -> q.copy(
                        type = com.linguaflow.app.domain.model.QuestionType.FILL_BLANK,
                        prompt = if (q.prompt.isNotBlank() && !q.prompt.startsWith("Pronuncia")) q.prompt else "Traduce al inglés: ${q.correctAnswer}"
                    )
                }
            }
        }

        return candidateQuestions.shuffled().take(limit)
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
