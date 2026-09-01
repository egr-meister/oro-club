package com.oroclub.game.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.data.GameRepository
import com.oroclub.game.domain.CardModel
import com.oroclub.game.domain.GameLogic
import com.oroclub.game.domain.LevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** A card as the UI sees it. */
data class CardUi(
    val id: Int,
    val itemIndex: Int,
    val faceUp: Boolean,
    val matched: Boolean
)

enum class PairsStatus { PLAYING, WON, LOST }

/** Immutable state of the Matching Pairs board. */
data class PairsUiState(
    val available: Boolean = true,
    val level: Int = 1,
    val columns: Int = 2,
    val totalPairs: Int = 2,
    val matchedPairs: Int = 0,
    val secondsLeft: Int = 0,
    val totalSeconds: Int = 1,
    val cards: List<CardUi> = emptyList(),
    val paused: Boolean = false,
    val status: PairsStatus = PairsStatus.PLAYING
) {
    val timeProgress: Float
        get() = if (totalSeconds <= 0) 0f else secondsLeft.toFloat() / totalSeconds.toFloat()
}

class PairsViewModel(
    private val repository: GameRepository,
    private val level: Int
) : ViewModel() {

    private val config: LevelConfig? = GameLogic.levelConfig(level)

    private val _uiState = MutableStateFlow(freshState())
    val uiState: StateFlow<PairsUiState> = _uiState.asStateFlow()

    /** Ids of the face-up, not-yet-resolved cards. Never longer than 2. */
    private var pending: List<Int> = emptyList()
    private var resolving: Boolean = false

    private var timerJob: Job? = null
    private var resolveJob: Job? = null

    init {
        if (config != null) startTimer()
    }

    private fun freshState(): PairsUiState {
        val safeConfig = config ?: return PairsUiState(available = false)
        val deck: List<CardModel> = GameLogic.buildDeck(safeConfig.pairs)
        return PairsUiState(
            available = true,
            level = safeConfig.level,
            columns = safeConfig.columns,
            totalPairs = safeConfig.pairs,
            matchedPairs = 0,
            secondsLeft = safeConfig.seconds,
            totalSeconds = safeConfig.seconds,
            cards = deck.map { CardUi(id = it.id, itemIndex = it.itemIndex, faceUp = false, matched = false) },
            paused = false,
            status = PairsStatus.PLAYING
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val state = _uiState.value
                if (state.paused || state.status != PairsStatus.PLAYING) continue
                val next = state.secondsLeft - 1
                if (next <= 0) {
                    _uiState.update { it.copy(secondsLeft = 0) }
                    // This coroutine IS the timer. Drop the handle first, so
                    // finish() cannot cancel the job it is currently running in
                    // (which would kill the save and the LOST transition).
                    timerJob = null
                    finish(won = false)
                    return@launch
                }
                _uiState.update { it.copy(secondsLeft = next) }
            }
        }
    }

    fun flip(cardId: Int) {
        val state = _uiState.value
        if (!state.available || state.paused || state.status != PairsStatus.PLAYING) return
        if (resolving || pending.size >= 2) return

        val card = state.cards.firstOrNull { it.id == cardId } ?: return
        if (card.matched || card.faceUp) return

        SoundManager.click()
        pending = pending + cardId
        _uiState.update { current ->
            current.copy(cards = current.cards.map { if (it.id == cardId) it.copy(faceUp = true) else it })
        }

        if (pending.size == 2) resolvePair()
    }

    private fun resolvePair() {
        val ids = pending
        resolving = true
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            val cards = _uiState.value.cards
            val first = cards.firstOrNull { it.id == ids[0] }
            val second = cards.firstOrNull { it.id == ids[1] }
            val isMatch = first != null && second != null && first.itemIndex == second.itemIndex

            if (isMatch) {
                delay(MATCH_HOLD_MILLIS)
                SoundManager.play(SoundManager.Sfx.MATCH)
                val matched = _uiState.value.matchedPairs + 1
                _uiState.update { current ->
                    current.copy(
                        matchedPairs = matched,
                        cards = current.cards.map {
                            if (it.id in ids) it.copy(faceUp = true, matched = true) else it
                        }
                    )
                }
                pending = emptyList()
                resolving = false
                if (matched >= _uiState.value.totalPairs) finish(won = true)
            } else {
                delay(MISMATCH_HOLD_MILLIS)
                // Honour a pause taken during the reveal window.
                while (_uiState.value.paused) delay(80)
                _uiState.update { current ->
                    current.copy(
                        cards = current.cards.map {
                            if (it.id in ids && !it.matched) it.copy(faceUp = false) else it
                        }
                    )
                }
                pending = emptyList()
                resolving = false
            }
        }
    }

    private suspend fun finish(won: Boolean) {
        timerJob?.cancel()
        timerJob = null
        val state = _uiState.value
        repository.savePairsBest(state.level, state.matchedPairs)
        SoundManager.play(if (won) SoundManager.Sfx.WIN else SoundManager.Sfx.LOSE)
        _uiState.update { it.copy(status = if (won) PairsStatus.WON else PairsStatus.LOST, paused = false) }
    }

    fun pause() {
        if (_uiState.value.status != PairsStatus.PLAYING) return
        SoundManager.click()
        _uiState.update { it.copy(paused = true) }
    }

    /**
     * Pauses without the click sound. Used when the screen leaves the
     * foreground, so a phone call or a trip to the launcher can never run the
     * clock down behind the player's back.
     */
    fun pauseForBackground() {
        if (_uiState.value.status != PairsStatus.PLAYING) return
        _uiState.update { it.copy(paused = true) }
    }

    fun resume() {
        SoundManager.click()
        _uiState.update { it.copy(paused = false) }
    }

    fun restart() {
        SoundManager.click()
        resolveJob?.cancel()
        timerJob?.cancel()
        pending = emptyList()
        resolving = false
        _uiState.value = freshState()
        if (config != null) startTimer()
    }

    override fun onCleared() {
        timerJob?.cancel()
        resolveJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val repository: GameRepository,
        private val level: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PairsViewModel::class.java)) {
                "Unknown ViewModel: ${modelClass.name}"
            }
            return PairsViewModel(repository, level) as T
        }
    }

    private companion object {
        const val MATCH_HOLD_MILLIS = 260L
        const val MISMATCH_HOLD_MILLIS = 700L
    }
}
