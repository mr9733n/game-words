package com.example.partywordgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.partywordgame.core.GameManager
import com.example.partywordgame.models.GameSettings
import com.example.partywordgame.models.GameState
import com.example.partywordgame.models.WordState
import com.example.partywordgame.persistence.GamePersistence
import com.example.partywordgame.persistence.GamePersistenceImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameManager = GameManager()
    private val persistence: GamePersistence = GamePersistenceImpl(application)
    
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState
    
    private val _screenState = MutableStateFlow(ScreenState.HOME)
    val screenState: StateFlow<ScreenState> = _screenState
    
    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft
    
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning
    
    private var timerJob: kotlinx.coroutines.Job? = null
    
    init {
        viewModelScope.launch {
            loadSavedGame()
        }
    }
    
    fun showHomeScreen() {
        _screenState.value = ScreenState.HOME
    }
    
    fun showSetupScreen() {
        _screenState.value = ScreenState.SETUP
    }
    
    fun startNewGame(settings: GameSettings) {
        viewModelScope.launch {
            try {
                // val activeWords = persistence.getActiveWords()
                val activeWords = emptySet<String>()
                val gameState = gameManager.startNewGame(settings, activeWords)
                _gameState.value = gameState
                
                // Add words to active words set
                val wordSetText = gameState.wordBulk.map { it.text }.toSet()
                // persistence.addActiveWords(wordSetText)
                persistence.saveGameState(gameState)
                
                _screenState.value = ScreenState.GAME
                resetTimer(gameState.settings.turnDurationSeconds)
            } catch (e: Exception) {
                // Handle insufficient words exception
                // In a real app, we would show a dialog to the user
            }
        }
    }
    
    fun resumeGame() {
        _screenState.value = ScreenState.GAME
        _gameState.value?.let { gameState ->
            resetTimer(gameState.settings.turnDurationSeconds)
        }
    }
    
    fun canResumeGame(): Boolean {
        return _gameState.value != null && _gameState.value?.status != com.example.partywordgame.models.GameStatus.FINISHED
    }

    fun startTurn() {
        val currentState = _gameState.value ?: return

        val cleanedWords = currentState.wordBulk.map {
            if (it.state == WordState.IN_TURN) {
                it.copy(state = WordState.AVAILABLE)
            } else {
                it
            }
        }

        val wordIndex = if (cleanedWords[currentState.current.wordIndex].state != WordState.GUESSED) {
            currentState.current.wordIndex
        } else {
            cleanedWords.indexOfFirst { it.state != WordState.GUESSED }
        }

        if (wordIndex == -1) {
            _screenState.value = ScreenState.SUMMARY
            return
        }

        val updatedWords = cleanedWords.toMutableList().apply {
            this[wordIndex] = this[wordIndex].copy(state = WordState.IN_TURN)
        }

        val updatedState = currentState.copy(
            wordBulk = updatedWords,
            current = currentState.current.copy(wordIndex = wordIndex)
        )

        _gameState.value = updatedState

        viewModelScope.launch {
            persistence.saveGameState(updatedState)
        }

        startTimer()
    }

    fun markWordAsGuessed() {
        val currentState = _gameState.value ?: return

        val currentWordIndex = currentState.current.wordIndex
        val currentTeamIndex = currentState.current.teamIndex

        val updatedTeams = currentState.teams.toMutableList().apply {
            this[currentTeamIndex] = this[currentTeamIndex].copy(
                score = this[currentTeamIndex].score + 1
            )
        }

        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(
                state = WordState.GUESSED
            )
        }

        val nextWordIndex = updatedWords.indexOfFirst {
            it.state != WordState.GUESSED
        }

        val newState = if (nextWordIndex == -1) {
            currentState.copy(
                teams = updatedTeams,
                wordBulk = updatedWords
            )
        } else {
            val finalWords = updatedWords.toMutableList().apply {
                this[nextWordIndex] = this[nextWordIndex].copy(
                    state = WordState.IN_TURN
                )
            }

            currentState.copy(
                teams = updatedTeams,
                wordBulk = finalWords,
                current = currentState.current.copy(
                    wordIndex = nextWordIndex
                )
            )
        }

        _gameState.value = newState

        if (newState.wordBulk.all { it.state == WordState.GUESSED }) {
            stopTimer()
            _screenState.value = ScreenState.SUMMARY
        }

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun pauseGame() {
        stopTimer()
    }

    fun restartRound() {
        val currentState = _gameState.value ?: return

        val resetWords = currentState.wordBulk.map {
            it.copy(state = WordState.AVAILABLE)
        }

        val newState = currentState.copy(
            wordBulk = resetWords,
            current = currentState.current.copy(
                teamIndex = 0,
                wordIndex = 0
            )
        )

        _gameState.value = newState
        _screenState.value = ScreenState.GAME
        resetTimer(newState.settings.turnDurationSeconds)

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun restartGame() {
        val currentState = _gameState.value ?: return

        val resetWords = currentState.wordBulk.map {
            it.copy(state = WordState.AVAILABLE)
        }

        val resetTeams = currentState.teams.map {
            it.copy(score = 0)
        }

        val newState = currentState.copy(
            status = com.example.partywordgame.models.GameStatus.ACTIVE,
            teams = resetTeams,
            wordBulk = resetWords,
            current = currentState.current.copy(
                round = 1,
                teamIndex = 0,
                wordIndex = 0
            )
        )

        _gameState.value = newState
        _screenState.value = ScreenState.GAME
        resetTimer(newState.settings.turnDurationSeconds)

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun moveToNextTurn() {
        finishCurrentTurn()
    }

    private fun finishCurrentTurn() {
        val currentState = _gameState.value ?: return

        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null

        val cleanedWords = currentState.wordBulk.map {
            if (it.state == WordState.IN_TURN) {
                it.copy(state = WordState.AVAILABLE)
            } else {
                it
            }
        }

        if (cleanedWords.all { it.state == WordState.GUESSED }) {
            val roundFinishedState = currentState.copy(wordBulk = cleanedWords)
            _gameState.value = roundFinishedState
            _screenState.value = ScreenState.SUMMARY

            viewModelScope.launch {
                persistence.saveGameState(roundFinishedState)
            }
            return
        }

        val nextTeamIndex = (currentState.current.teamIndex + 1) % currentState.teams.size

        val nextWordIndex = cleanedWords.indexOfFirst {
            it.state != WordState.GUESSED
        }.coerceAtLeast(0)

        val newState = currentState.copy(
            wordBulk = cleanedWords,
            current = currentState.current.copy(
                teamIndex = nextTeamIndex,
                wordIndex = nextWordIndex
            )
        )

        _gameState.value = newState
        _screenState.value = ScreenState.GAME
        _timeLeft.value = newState.settings.turnDurationSeconds

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun continueGame() {
        val currentState = _gameState.value ?: return

        val allWordsGuessed = currentState.wordBulk.all {
            it.state == WordState.GUESSED
        }

        if (!allWordsGuessed) {
            _screenState.value = ScreenState.GAME
            resetTimer(currentState.settings.turnDurationSeconds)
            return
        }

        val isFinalRound = currentState.current.round >= currentState.settings.roundCount

        if (isFinalRound) {
            _screenState.value = ScreenState.FINAL
            return
        }

        val minScore = currentState.teams.minOf { it.score }
        val nextRoundStartingTeamIndex = currentState.teams.indexOfFirst {
            it.score == minScore
        }

        val resetWords = currentState.wordBulk.map {
            it.copy(state = WordState.AVAILABLE)
        }

        val newState = currentState.copy(
            wordBulk = resetWords,
            current = currentState.current.copy(
                round = currentState.current.round + 1,
                teamIndex = nextRoundStartingTeamIndex,
                wordIndex = 0
            )
        )

        _gameState.value = newState
        _screenState.value = ScreenState.GAME
        resetTimer(newState.settings.turnDurationSeconds)

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun skipWord() {
        val currentState = _gameState.value ?: return

        val currentWordIndex = currentState.current.wordIndex

        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(state = WordState.AVAILABLE)
        }

        val nextWordIndex = updatedWords
            .mapIndexedNotNull { index, word ->
                if (word.state != WordState.GUESSED && index != currentWordIndex) index else null
            }
            .randomOrNull()
            ?: currentWordIndex

        val finalWords = updatedWords.toMutableList().apply {
            this[nextWordIndex] = this[nextWordIndex].copy(state = WordState.IN_TURN)
        }

        val newState = currentState.copy(
            wordBulk = finalWords,
            current = currentState.current.copy(
                wordIndex = nextWordIndex
            )
        )

        _gameState.value = newState

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun finishGame() {
        val currentState = _gameState.value ?: return

        val finalState = currentState.copy(
            status = com.example.partywordgame.models.GameStatus.FINISHED
        )

        _gameState.value = finalState
        _screenState.value = ScreenState.FINAL
        stopTimer()

        viewModelScope.launch {
            val wordSetText = finalState.wordBulk.map { it.text }.toSet()
            persistence.removeActiveWords(wordSetText)
            persistence.saveGameState(finalState)
        }
    }
    
    private fun startTimer() {
        stopTimer() // Make sure any existing timer is stopped
        
        _gameState.value?.let { gameState ->
            _timeLeft.value = gameState.settings.turnDurationSeconds
            _isTimerRunning.value = true
            
            timerJob = viewModelScope.launch {
                while (_timeLeft.value > 0 && _isTimerRunning.value) {
                    kotlinx.coroutines.delay(1000)
                    _timeLeft.value--
                }
                
                if (_timeLeft.value == 0) {
                    // Time's up, move to next turn
                    moveToNextTurn()
                }
            }
        }
    }
    
    private fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }
    
    private fun resetTimer(duration: Int) {
        stopTimer()
        _timeLeft.value = duration
    }
    
    private suspend fun loadSavedGame() {
        try {
            val savedState = persistence.loadGameState()
            if (savedState != null && savedState.status != com.example.partywordgame.models.GameStatus.FINISHED) {
                _gameState.value = savedState
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
