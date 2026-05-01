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
                val activeWords = persistence.getActiveWords()
                val gameState = gameManager.startNewGame(settings, activeWords)
                _gameState.value = gameState
                
                // Add words to active words set
                val wordSetText = gameState.wordBulk.map { it.text }.toSet()
                persistence.addActiveWords(wordSetText)
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
        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentState.current.wordIndex] = this[currentState.current.wordIndex].copy(state = com.example.partywordgame.models.WordState.IN_TURN)
        }
        
        val updatedState = currentState.copy(wordBulk = updatedWords)
        _gameState.value = updatedState
        startTimer()
    }
    
    fun markWordAsGuessed() {
        stopTimer()
        val newState = gameManager.markWordAsGuessed()
        _gameState.value = newState
        
        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
        
        // Check if all words are guessed
        if (gameManager.areAllWordsGuessed()) {
            _screenState.value = ScreenState.SUMMARY
        }
    }
    
    fun moveToNextTurn() {
        stopTimer()
        val newState = gameManager.moveToNextTurn()
        _gameState.value = newState
        
        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }
    
    fun pauseGame() {
        stopTimer()
        _screenState.value = ScreenState.HOME
    }
    
    fun continueGame() {
        // Check if we should move to next round or finish the game
        _gameState.value?.let { gameState ->
            if (gameManager.areAllWordsGuessed()) {
                if (gameManager.isFinalRound()) {
                    _screenState.value = ScreenState.FINAL
                } else {
                    val newState = gameManager.moveToNextRound()
                    _gameState.value = newState
                    _screenState.value = ScreenState.GAME
                    
                    viewModelScope.launch {
                        persistence.saveGameState(newState)
                    }
                }
            } else {
                _screenState.value = ScreenState.GAME
                resetTimer(gameState.settings.turnDurationSeconds)
            }
        }
    }
    
    fun finishGame() {
        val newState = gameManager.finishGame()
        _gameState.value = newState
        _screenState.value = ScreenState.FINAL
        
        viewModelScope.launch {
            // Remove words from active words since game is finished
            val wordSetText = newState.wordBulk.map { it.text }.toSet()
            persistence.removeActiveWords(wordSetText)
            persistence.saveGameState(newState)
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
