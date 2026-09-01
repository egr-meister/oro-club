package com.oroclub.game.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.data.GameRepository
import com.oroclub.game.domain.QuizCategory
import com.oroclub.game.domain.QuizContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable state of the quiz screen. */
data class QuizUiState(
    val available: Boolean = true,
    val categoryTitle: String = "",
    val badgeItemIndex: Int = 1,
    val questionIndex: Int = 0,
    val questionCount: Int = QuizContent.QUESTIONS_PER_CATEGORY,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1,
    val selectedIndex: Int? = null,
    val locked: Boolean = false,
    val score: Int = 0,
    val paused: Boolean = false,
    val finished: Boolean = false
) {
    val progress: Float
        get() = if (questionCount <= 0) 0f else questionIndex.toFloat() / questionCount.toFloat()

    /** Position shown to the player, 1-based. */
    val displayPosition: Int get() = (questionIndex + 1).coerceAtMost(questionCount)
}

class QuizViewModel(
    private val repository: GameRepository,
    private val categoryId: String
) : ViewModel() {

    private val category: QuizCategory? = QuizContent.categoryById(categoryId)

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var advanceJob: Job? = null

    private fun initialState(): QuizUiState {
        val safeCategory = category ?: return QuizUiState(available = false)
        val first = safeCategory.questions.first()
        return QuizUiState(
            available = true,
            categoryTitle = safeCategory.title,
            badgeItemIndex = safeCategory.badgeItemIndex,
            questionIndex = 0,
            questionCount = safeCategory.questions.size,
            questionText = first.text,
            options = first.options,
            correctIndex = first.correctIndex,
            selectedIndex = null,
            locked = false,
            score = 0,
            paused = false,
            finished = false
        )
    }

    fun selectAnswer(index: Int) {
        val safeCategory = category ?: return
        val state = _uiState.value
        if (state.locked || state.paused || state.finished) return
        if (index !in state.options.indices) return

        val correct = index == state.correctIndex
        SoundManager.play(if (correct) SoundManager.Sfx.CORRECT else SoundManager.Sfx.WRONG)

        _uiState.update {
            it.copy(
                selectedIndex = index,
                locked = true,
                score = if (correct) it.score + 1 else it.score
            )
        }

        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(REVEAL_MILLIS)
            // The player may have paused during the reveal: wait it out.
            while (_uiState.value.paused) delay(80)
            advance(safeCategory)
        }
    }

    private suspend fun advance(safeCategory: QuizCategory) {
        val state = _uiState.value
        val next = state.questionIndex + 1
        if (next >= safeCategory.questions.size) {
            repository.saveQuizBest(safeCategory.id, state.score)
            _uiState.update { it.copy(finished = true, locked = true, selectedIndex = null) }
            return
        }
        val question = safeCategory.questions[next]
        _uiState.update {
            it.copy(
                questionIndex = next,
                questionText = question.text,
                options = question.options,
                correctIndex = question.correctIndex,
                selectedIndex = null,
                locked = false
            )
        }
    }

    fun pause() {
        if (_uiState.value.finished) return
        SoundManager.click()
        _uiState.update { it.copy(paused = true) }
    }

    fun resume() {
        SoundManager.click()
        _uiState.update { it.copy(paused = false) }
    }

    fun restart() {
        SoundManager.click()
        advanceJob?.cancel()
        advanceJob = null
        _uiState.value = initialState()
    }

    override fun onCleared() {
        advanceJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val repository: GameRepository,
        private val categoryId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return QuizViewModel(repository, categoryId) as T
        }
    }

    private companion object {
        const val REVEAL_MILLIS = 750L
    }
}
