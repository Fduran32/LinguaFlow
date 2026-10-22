package com.linguaflow.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.linguaflow.app.domain.model.*
import com.linguaflow.app.ui.screens.auth.AuthScreen
import com.linguaflow.app.ui.screens.lesson.LessonSessionScreen
import com.linguaflow.app.ui.screens.path.LearningPathScreen
import com.linguaflow.app.ui.screens.path.LearningPathViewModel
import com.linguaflow.app.ui.screens.placement.PlacementTestScreen
import com.linguaflow.app.ui.screens.profile.ProfileScreen
import com.linguaflow.app.ui.screens.review.MistakesReviewScreen
import com.linguaflow.app.ui.theme.LinguaFlowTheme
import kotlinx.coroutines.launch
import java.util.*

enum class AppNavState {
    LEARNING_PATH,
    LESSON_SESSION,
    PLACEMENT_TEST,
    MISTAKES_REVIEW,
    PROFILE
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val viewModel: LearningPathViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            LinguaFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinguaFlowApp(
                        viewModel = viewModel,
                        onPlayAudio = { text, localeStr ->
                            speakText(text, localeStr)
                        }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    private fun speakText(text: String, localeStr: String) {
        val locale = when (localeStr) {
            "it-IT" -> Locale.ITALIAN
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.US
        }
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun LinguaFlowApp(
    viewModel: LearningPathViewModel,
    onPlayAudio: (String, String) -> Unit
) {
    var navState by remember { mutableStateOf(AppNavState.LEARNING_PATH) }
    var currentSelectedLevel by remember { mutableStateOf<Level?>(null) }
    var activeQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isMistakeReviewMode by remember { mutableStateOf(false) }
    var mistakeQuestionsList by remember { mutableStateOf<List<Question>>(emptyList()) }

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Si no hay usuario activo autenticado, mostramos la pantalla de Autenticación
    if (uiState.currentUser == null) {
        AuthScreen(
            onLoginSuccess = {
                navState = AppNavState.LEARNING_PATH
            },
            onRegister = { username, password, avatar, isCustomPhoto ->
                viewModel.registerUser(username, password, avatar, isCustomPhoto)
            },
            onLogin = { username, password ->
                viewModel.loginUser(username, password)
            }
        )
        return
    }

    when (navState) {
        AppNavState.LEARNING_PATH -> {
            LearningPathScreen(
                currentUser = uiState.currentUser,
                currentLanguageName = uiState.currentLanguage.name,
                currentFlagEmoji = uiState.currentLanguage.flagEmoji,
                availableLanguages = uiState.languages,
                levels = uiState.levels,
                userAssignedLevel = uiState.userAssignedLevel,
                selectedViewingLevel = uiState.selectedViewingLevel,
                availablePracticeLevels = uiState.availablePracticeLevels,
                pendingMistakesCount = uiState.pendingMistakesCount,
                isLoading = uiState.isLoading,
                onSelectLanguage = { langId ->
                    viewModel.switchLanguage(langId)
                },
                onSelectViewingLevel = { cefr ->
                    viewModel.selectViewingLevel(cefr)
                },
                onOpenMistakesReview = {
                    coroutineScope.launch {
                        mistakeQuestionsList = viewModel.getPendingMistakeQuestions()
                        navState = AppNavState.MISTAKES_REVIEW
                    }
                },
                onOpenProfile = {
                    navState = AppNavState.PROFILE
                },
                onStartLevel = { level ->
                    currentSelectedLevel = level
                    isMistakeReviewMode = false
                    coroutineScope.launch {
                        // Cargar preguntas desde Room SQLite
                        val questions = viewModel.getQuestionsForLevel(level.id)
                        activeQuestions = questions
                        navState = AppNavState.LESSON_SESSION
                    }
                },
                onTakePlacementTest = {
                    navState = AppNavState.PLACEMENT_TEST
                }
            )
        }

        AppNavState.LESSON_SESSION -> {
            val targetLevel = currentSelectedLevel ?: uiState.levels.firstOrNull()
            val levelTitle = if (isMistakeReviewMode) "Repaso de Errores" else (targetLevel?.title ?: "Lección")

            LessonSessionScreen(
                levelTitle = levelTitle,
                questions = activeQuestions,
                ttsLocaleStr = uiState.currentLanguage.ttsLocale,
                onPlayAudio = onPlayAudio,
                onWrongAnswer = { questionId, wrongAnswer ->
                    viewModel.recordMistake(questionId, wrongAnswer)
                },
                onCorrectAnswer = { questionId ->
                    if (isMistakeReviewMode) {
                        viewModel.resolveMistake(questionId)
                    }
                },
                onLessonFinished = { passed, scorePercentage ->
                    if (passed && targetLevel != null && !isMistakeReviewMode) {
                        viewModel.onLevelCompleted(targetLevel.id, scorePercentage)
                    }
                    isMistakeReviewMode = false
                    navState = AppNavState.LEARNING_PATH
                },
                onClose = {
                    isMistakeReviewMode = false
                    navState = AppNavState.LEARNING_PATH
                }
            )
        }

        AppNavState.PLACEMENT_TEST -> {
            PlacementTestScreen(
                languageId = uiState.currentLanguage.id,
                languageName = uiState.currentLanguage.name,
                currentAssignedCefr = uiState.userAssignedLevel,
                onPlacementFinished = { startingLevelOrder, cefrCode, score ->
                    viewModel.onPlacementTestFinished(startingLevelOrder, cefrCode, score)
                    navState = AppNavState.LEARNING_PATH
                },
                onClose = {
                    navState = AppNavState.LEARNING_PATH
                }
            )
        }

        AppNavState.MISTAKES_REVIEW -> {
            MistakesReviewScreen(
                mistakes = mistakeQuestionsList,
                onStartReviewSession = {
                    activeQuestions = mistakeQuestionsList
                    isMistakeReviewMode = true
                    navState = AppNavState.LESSON_SESSION
                },
                onBack = {
                    navState = AppNavState.LEARNING_PATH
                }
            )
        }

        AppNavState.PROFILE -> {
            val currentUser = uiState.currentUser
            if (currentUser != null) {
                ProfileScreen(
                    user = currentUser,
                    languages = uiState.languages,
                    languageLevels = uiState.allLanguageLevels,
                    onUpdateProfile = { newName, newAvatar, isPhoto ->
                        viewModel.updateProfile(newName, newAvatar, isPhoto)
                    },
                    onLogout = {
                        viewModel.logout()
                        navState = AppNavState.LEARNING_PATH
                    },
                    onBack = {
                        navState = AppNavState.LEARNING_PATH
                    }
                )
            }
        }
    }
}
