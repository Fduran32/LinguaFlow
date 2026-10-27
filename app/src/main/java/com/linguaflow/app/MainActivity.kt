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
import com.linguaflow.app.ui.screens.home.HomeScreen
import com.linguaflow.app.ui.screens.lesson.LessonSessionScreen
import com.linguaflow.app.ui.screens.path.LearningPathScreen
import com.linguaflow.app.ui.screens.path.LearningPathViewModel
import com.linguaflow.app.ui.screens.placement.PlacementTestScreen
import com.linguaflow.app.ui.screens.practice.PracticeHubScreen
import com.linguaflow.app.ui.screens.practice.PracticeSessionScreen
import com.linguaflow.app.ui.screens.practice.PracticeSkillMode
import com.linguaflow.app.ui.screens.profile.ProfileScreen
import com.linguaflow.app.ui.screens.review.MistakesReviewScreen
import com.linguaflow.app.ui.theme.LinguaFlowTheme
import kotlinx.coroutines.launch
import java.util.*

enum class AppNavState {
    HOME,
    LEARNING_PATH,
    LESSON_SESSION,
    PLACEMENT_TEST,
    MISTAKES_REVIEW,
    PROFILE,
    PRACTICE_HUB,
    PRACTICE_SESSION
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
                        onPlayAudio = { text, localeStr, isSlow ->
                            speakText(text, localeStr, isSlow)
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

    private fun speakText(text: String, localeStr: String, isSlow: Boolean = false) {
        val locale = when (localeStr) {
            "it-IT" -> Locale.ITALIAN
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.US
        }
        tts?.language = locale
        tts?.setSpeechRate(if (isSlow) 0.65f else 1.0f)
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
    onPlayAudio: (String, String, Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (uiState.currentUser == null) {
        AuthScreen(
            onLoginSuccess = {
                viewModel.navState = AppNavState.HOME
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

    when (viewModel.navState) {
        AppNavState.HOME -> {
            val currentUser = uiState.currentUser
            if (currentUser != null) {
                HomeScreen(
                    currentUser = currentUser,
                    currentLanguageName = uiState.currentLanguage.name,
                    currentFlagEmoji = uiState.currentLanguage.flagEmoji,
                    currentAssignedCefr = uiState.userAssignedLevel,
                    availableLanguages = uiState.languages,
                    pendingMistakesCount = uiState.pendingMistakesCount,
                    onSelectLanguage = { langId ->
                        viewModel.switchLanguage(langId)
                    },
                    onOpenProfile = {
                        viewModel.navState = AppNavState.PROFILE
                    },
                    onOpenMistakesReview = {
                        coroutineScope.launch {
                            viewModel.mistakeQuestionsList = viewModel.getPendingMistakeQuestions()
                            viewModel.navState = AppNavState.MISTAKES_REVIEW
                        }
                    },
                    onOpenLearningPath = {
                        viewModel.navState = AppNavState.LEARNING_PATH
                    },
                    onOpenPracticeHub = {
                        viewModel.navState = AppNavState.PRACTICE_HUB
                    }
                )
            }
        }

        AppNavState.LEARNING_PATH -> {
            LearningPathScreen(
                currentUser = uiState.currentUser,
                currentLanguageName = uiState.currentLanguage.name,
                currentFlagEmoji = uiState.currentLanguage.flagEmoji,
                ttsLocaleStr = uiState.currentLanguage.ttsLocale,
                availableLanguages = uiState.languages,
                levels = uiState.levels,
                userAssignedLevel = uiState.userAssignedLevel,
                selectedViewingLevel = uiState.selectedViewingLevel,
                availablePracticeLevels = uiState.availablePracticeLevels,
                pendingMistakesCount = uiState.pendingMistakesCount,
                isLoading = uiState.isLoading,
                onLoadGuideQuestions = { levelId ->
                    viewModel.getQuestionsForLevel(levelId)
                },
                onPlayAudio = { text, localeStr, isSlow ->
                    onPlayAudio(text, localeStr, isSlow)
                },
                onSelectLanguage = { langId ->
                    viewModel.switchLanguage(langId)
                },
                onSelectViewingLevel = { cefr ->
                    viewModel.selectViewingLevel(cefr)
                },
                onOpenMistakesReview = {
                    coroutineScope.launch {
                        viewModel.mistakeQuestionsList = viewModel.getPendingMistakeQuestions()
                        viewModel.navState = AppNavState.MISTAKES_REVIEW
                    }
                },
                onOpenProfile = {
                    viewModel.navState = AppNavState.PROFILE
                },
                onBackToHome = {
                    viewModel.navState = AppNavState.HOME
                },
                onStartLevel = { level ->
                    viewModel.currentSelectedLevel = level
                    viewModel.isMistakeReviewMode = false
                    coroutineScope.launch {
                        val questions = viewModel.getQuestionsForLevel(level.id).shuffled()
                        viewModel.activeQuestions = questions
                        viewModel.navState = AppNavState.LESSON_SESSION
                    }
                },
                onTakePlacementTest = {
                    viewModel.navState = AppNavState.PLACEMENT_TEST
                }
            )
        }

        AppNavState.PRACTICE_HUB -> {
            PracticeHubScreen(
                languageName = uiState.currentLanguage.name,
                flagEmoji = uiState.currentLanguage.flagEmoji,
                currentCefr = uiState.userAssignedLevel,
                pendingMistakesCount = uiState.pendingMistakesCount,
                onStartPractice = { mode ->
                    viewModel.selectedPracticeMode = mode
                    coroutineScope.launch {
                        viewModel.practiceQuestionsList = viewModel.getPracticeQuestions(mode, 10)
                        viewModel.navState = AppNavState.PRACTICE_SESSION
                    }
                },
                onStartMistakesReview = {
                    coroutineScope.launch {
                        viewModel.mistakeQuestionsList = viewModel.getPendingMistakeQuestions()
                        viewModel.navState = AppNavState.MISTAKES_REVIEW
                    }
                },
                onBack = {
                    viewModel.navState = AppNavState.HOME
                }
            )
        }

        AppNavState.PRACTICE_SESSION -> {
            PracticeSessionScreen(
                skillMode = viewModel.selectedPracticeMode,
                questions = viewModel.practiceQuestionsList,
                ttsLocaleStr = uiState.currentLanguage.ttsLocale,
                onPlayAudio = { text, isSlow ->
                    onPlayAudio(text, uiState.currentLanguage.ttsLocale, isSlow)
                },
                onWrongAnswer = { questionId, wrongAnswer ->
                    viewModel.recordMistake(questionId, wrongAnswer)
                },
                onFinished = {
                    viewModel.navState = AppNavState.PRACTICE_HUB
                },
                onClose = {
                    viewModel.navState = AppNavState.PRACTICE_HUB
                }
            )
        }

        AppNavState.LESSON_SESSION -> {
            val targetLevel = viewModel.currentSelectedLevel ?: uiState.levels.firstOrNull()
            val levelTitle = if (viewModel.isMistakeReviewMode) "Repaso de Errores" else (targetLevel?.title ?: "Lección")

            LessonSessionScreen(
                levelTitle = levelTitle,
                questions = viewModel.activeQuestions,
                ttsLocaleStr = uiState.currentLanguage.ttsLocale,
                onPlayAudio = { text, localeStr, isSlow ->
                    onPlayAudio(text, localeStr, isSlow)
                },
                onWrongAnswer = { questionId, wrongAnswer ->
                    viewModel.recordMistake(questionId, wrongAnswer)
                },
                onCorrectAnswer = { questionId ->
                    if (viewModel.isMistakeReviewMode) {
                        viewModel.resolveMistake(questionId)
                    }
                },
                onLessonFinished = { passed, scorePercentage ->
                    if (passed && targetLevel != null && !viewModel.isMistakeReviewMode) {
                        viewModel.onLevelCompleted(targetLevel.id, scorePercentage)
                    }
                    viewModel.isMistakeReviewMode = false
                    viewModel.navState = AppNavState.LEARNING_PATH
                },
                onClose = {
                    viewModel.isMistakeReviewMode = false
                    viewModel.navState = AppNavState.LEARNING_PATH
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
                    viewModel.navState = AppNavState.LEARNING_PATH
                },
                onClose = {
                    viewModel.navState = AppNavState.LEARNING_PATH
                }
            )
        }

        AppNavState.MISTAKES_REVIEW -> {
            MistakesReviewScreen(
                mistakes = viewModel.mistakeQuestionsList,
                onStartReviewSession = {
                    viewModel.activeQuestions = viewModel.mistakeQuestionsList
                    viewModel.isMistakeReviewMode = true
                    viewModel.navState = AppNavState.LESSON_SESSION
                },
                onBack = {
                    viewModel.navState = AppNavState.HOME
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
                        viewModel.navState = AppNavState.HOME
                    },
                    onBack = {
                        viewModel.navState = AppNavState.HOME
                    }
                )
            }
        }
    }
}
