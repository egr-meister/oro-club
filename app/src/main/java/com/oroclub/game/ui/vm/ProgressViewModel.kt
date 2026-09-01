package com.oroclub.game.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.data.GameRepository
import com.oroclub.game.domain.GameLogic
import com.oroclub.game.domain.QuizContent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One row of the results summary. */
data class CategoryProgress(
    val id: String,
    val title: String,
    val badgeItemIndex: Int,
    val best: Int,
    val total: Int
)

/** Immutable snapshot consumed by the menu, levels, results and settings screens. */
data class ProgressUiState(
    val loaded: Boolean = false,
    val soundEnabled: Boolean = true,
    val quizProgress: List<CategoryProgress> = emptyList(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val completedLevels: Int = 0,
    val totalLevels: Int = GameLogic.LEVEL_COUNT
) {
    fun isLevelUnlocked(level: Int): Boolean = GameLogic.isLevelUnlocked(level, pairsBest)
    fun isLevelCompleted(level: Int): Boolean = GameLogic.isLevelCompleted(level, pairsBest)
    fun bestPairs(level: Int): Int = pairsBest[level.toString()] ?: 0
}

class ProgressViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = repository.data
        .map { data ->
            ProgressUiState(
                loaded = true,
                soundEnabled = data.settings.soundEnabled,
                quizProgress = QuizContent.categories.map { category ->
                    CategoryProgress(
                        id = category.id,
                        title = category.title,
                        badgeItemIndex = category.badgeItemIndex,
                        best = (data.quizBest[category.id] ?: 0)
                            .coerceIn(0, QuizContent.QUESTIONS_PER_CATEGORY),
                        total = QuizContent.QUESTIONS_PER_CATEGORY
                    )
                },
                pairsBest = data.pairsBest,
                completedLevels = GameLogic.completedLevelCount(data.pairsBest),
                totalLevels = GameLogic.LEVEL_COUNT
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProgressUiState()
        )

    fun setSoundEnabled(enabled: Boolean) {
        SoundManager.setEnabled(enabled)
        SoundManager.click()
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun resetProgress() {
        SoundManager.click()
        viewModelScope.launch { repository.resetAll() }
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return ProgressViewModel(repository) as T
        }
    }
}
