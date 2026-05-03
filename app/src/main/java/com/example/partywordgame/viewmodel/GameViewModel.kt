package com.example.partywordgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.partywordgame.core.GameManager
import com.example.partywordgame.models.GameMode
import com.example.partywordgame.models.GameSettings
import com.example.partywordgame.models.GameState
import com.example.partywordgame.models.WordState
import com.example.partywordgame.data.local.WordEntity
import com.example.partywordgame.data.WordRepository
import com.example.partywordgame.persistence.GamePersistence
import com.example.partywordgame.persistence.GamePersistenceImpl
import com.example.partywordgame.models.GameRecord
import com.example.partywordgame.models.GameStatus
import com.example.partywordgame.models.TeamScore
import com.example.partywordgame.models.UsedWordRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val gameManager = GameManager(getApplication())
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

    private val _currentMode = MutableStateFlow(GameMode.NORMAL)
    val currentMode: StateFlow<GameMode> = _currentMode

    private val _records = MutableStateFlow<List<GameRecord>>(emptyList())
    val records: StateFlow<List<GameRecord>> = _records

    private val wordRepository = WordRepository(application.applicationContext)

    private val _wordSearchQuery = MutableStateFlow("")
    val wordSearchQuery: StateFlow<String> = _wordSearchQuery

    private val _wordDifficultyFilter = MutableStateFlow(setOf("easy", "medium", "hard"))
    val wordDifficultyFilter: StateFlow<Set<String>> = _wordDifficultyFilter

    private val _wordList = MutableStateFlow<List<WordEntity>>(emptyList())
    val wordList: StateFlow<List<WordEntity>> = _wordList

    private val _showDisabledWords = MutableStateFlow(false)
    val showDisabledWords: StateFlow<Boolean> = _showDisabledWords

    init {
        viewModelScope.launch {
            loadSavedGame()
        }
    }

    private suspend fun loadSavedGame() {
        try {
            val savedState = persistence.loadGameState()

            if (savedState != null && savedState.status != GameStatus.FINISHED) {
                _gameState.value = savedState
            }

            _records.value = persistence.getGameRecords()
            wordRepository.importDictionaryIfNeeded()

        } catch (e: Exception) {
            // Handle error
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Home Screen
     */
    fun showHomeScreen() {
        _screenState.value = ScreenState.HOME
    }

    fun showSettingsScreen() {
        _screenState.value = ScreenState.SETTINGS
    }

    fun showSetupScreen() {
        _screenState.value = ScreenState.SETUP
    }

    fun showRecordsScreen() {
        _screenState.value = ScreenState.RECORDS
    }

    /**
     * Resume Game
     */
    fun resumeGame() {
        _screenState.value = ScreenState.GAME
        _gameState.value?.let { gameState ->
            resetTimer(gameState.settings.turnDurationSeconds)
        }
    }

    fun canResumeGame(): Boolean {
        return _gameState.value != null && _gameState.value?.status != GameStatus.FINISHED
    }

    /**
     * Game mode
     *
    */
    fun setTestMode() {
        _currentMode.value = GameMode.TEST
    }

    fun setGameMode() {
        _currentMode.value = GameMode.NORMAL
    }

    /**
     * Game Setup
     */
    fun startNewGame(settings: GameSettings) {
        viewModelScope.launch {
            try {
                val mode = _currentMode.value

                val effectiveSettings = when (mode) {
                    GameMode.TEST -> settings.copy(
                        bulkSize = 5,
                        teamCount = 2,
                        roundCount = 2,
                        turnDurationSeconds = 15
                    )

                    GameMode.NORMAL -> settings
                }

                val activeWords = when (mode) {
                    GameMode.TEST -> emptySet()
                    GameMode.NORMAL -> persistence.getActiveWords()
                }

                val wordBulk = wordRepository.getWordBulk(
                    settings = effectiveSettings,
                    activeWords = activeWords,
                    mode = mode
                )

                val gameState = gameManager.startNewGame(
                    settings = effectiveSettings,
                    wordBulk = wordBulk
                )

                _gameState.value = gameState

                if (mode == GameMode.NORMAL) {
                    val wordSetText = gameState.wordBulk.map { it.text }.toSet()
                    persistence.addActiveWords(wordSetText)
                    persistence.saveGameState(gameState)
                }

                _screenState.value = ScreenState.GAME
                resetTimer(gameState.settings.turnDurationSeconds)

            } catch (e: Exception) {
                e.printStackTrace()

                _errorMessage.value = when {
                    e.message?.contains("Not enough words available.", ignoreCase = true) == true ->
                        "${e.message}. \nClear active words in Settings or reduce bulk size. \nGo to Settings -> Clear Active Words."
                    else ->
                        "Failed to start game: ${e.message ?: "unknown error"}"
                }
            }
        }
    }

    /**
     * Settings
     */
    fun resetSavedState() {
        viewModelScope.launch {
            persistence.clearGameState()
            _gameState.value = null
            _screenState.value = ScreenState.HOME
        }
    }

    fun clearActiveWords() {
        viewModelScope.launch {
            val activeWords = persistence.getActiveWords()
            persistence.removeActiveWords(activeWords)
        }
    }

    fun importDictionary() {
        viewModelScope.launch {
            try {
                val count = wordRepository.importDefaultDictionaryFromAssets()
                _errorMessage.value = "Dictionary imported: $count words"
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to import dictionary: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun clearDictionary() {
        viewModelScope.launch {
            try {
                wordRepository.clearDictionary()
                _errorMessage.value = "Dictionary cleared"
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to clear dictionary: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun showDictionaryStats() {
        viewModelScope.launch {
            try {
                val total = wordRepository.countAllWords()
                val enabled = wordRepository.countEnabledWords()
                val disabled = wordRepository.countDisabledWords()
                val active = persistence.getActiveWords().size

                _errorMessage.value = """
                Dictionary Info

                Total words: $total
                Enabled: $enabled
                Disabled: $disabled
                Active words: $active

                Source:
                OpenRussian / Badestrand russian-dictionary

                License:
                CC-BY-SA-4.0
            """.trimIndent()

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load dictionary stats: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun clearRecords() {
        viewModelScope.launch {
            try {
                persistence.clearGameRecords()
                _records.value = emptyList()
                _errorMessage.value = "Records cleared"
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to clear records: ${e.message ?: "unknown error"}"
            }
        }
    }

    /**
     * In Game
     */
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
            current = currentState.current.copy(
                wordIndex = wordIndex,
                skippedWordIdsInTurn = emptyList()
            )
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
        val skippedIds = currentState.current.skippedWordIdsInTurn

        val updatedTeams = currentState.teams.toMutableList().apply {
            this[currentTeamIndex] = this[currentTeamIndex].copy(
                score = this[currentTeamIndex].score + 1,
                roundScore = this[currentTeamIndex].roundScore + 1
            )
        }

        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(
                state = WordState.GUESSED
            )
        }

        val allWordsGuessed = updatedWords.all {
            it.state == WordState.GUESSED
        }

        if (allWordsGuessed) {
            val roundFinishedState = currentState.copy(
                teams = updatedTeams,
                wordBulk = updatedWords,
                current = currentState.current.copy(
                    skippedWordIdsInTurn = emptyList()
                )
            )

            _gameState.value = roundFinishedState
            stopTimer()
            _screenState.value = ScreenState.SUMMARY

            viewModelScope.launch {
                persistence.saveGameState(roundFinishedState)
            }

            return
        }

        val nextWordIndex = updatedWords.indexOfFirst {
            it.state != WordState.GUESSED && it.id !in skippedIds
        }

        if (nextWordIndex == -1) {
            val noWordsLeftForThisTurnState = currentState.copy(
                teams = updatedTeams,
                wordBulk = updatedWords,
                current = currentState.current.copy(
                    skippedWordIdsInTurn = skippedIds
                )
            )

            _gameState.value = noWordsLeftForThisTurnState

            viewModelScope.launch {
                persistence.saveGameState(noWordsLeftForThisTurnState)
            }

            finishCurrentTurn()
            return
        }

        val finalWords = updatedWords.toMutableList().apply {
            this[nextWordIndex] = this[nextWordIndex].copy(
                state = WordState.IN_TURN
            )
        }

        val newState = currentState.copy(
            teams = updatedTeams,
            wordBulk = finalWords,
            current = currentState.current.copy(
                wordIndex = nextWordIndex,
                skippedWordIdsInTurn = skippedIds
            )
        )

        _gameState.value = newState

        viewModelScope.launch {
            persistence.saveGameState(newState)
        }
    }

    fun pauseGame() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
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
        stopTimer()
        _isTimerRunning.value = false
        _screenState.value = ScreenState.SETUP
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
            val roundFinishedState = currentState.copy(
                wordBulk = cleanedWords,
                current = currentState.current.copy(
                    skippedWordIdsInTurn = emptyList()
                )
            )

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
                wordIndex = nextWordIndex,
                skippedWordIdsInTurn = emptyList()
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

        val minRoundScore = currentState.teams.minOf { it.roundScore }
        val nextRoundStartingTeamIndex = currentState.teams.indexOfFirst {
            it.roundScore == minRoundScore
        }

        val resetWords = currentState.wordBulk.map {
            it.copy(state = WordState.AVAILABLE)
        }

        val resetTeams = currentState.teams.map {
            it.copy(roundScore = 0)
        }

        val newState = currentState.copy(
            teams = resetTeams,
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
        val currentWord = currentState.wordBulk[currentWordIndex]

        val skippedIds = (
                currentState.current.skippedWordIdsInTurn + currentWord.id
                ).distinct()

        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(
                state = WordState.AVAILABLE
            )
        }

        val nextWordIndex = updatedWords
            .mapIndexedNotNull { index, word ->
                if (
                    word.state != WordState.GUESSED &&
                    word.id !in skippedIds &&
                    index != currentWordIndex
                ) index else null
            }
            .randomOrNull()

        if (nextWordIndex == null) {
            val newState = currentState.copy(
                wordBulk = updatedWords,
                current = currentState.current.copy(
                    skippedWordIdsInTurn = skippedIds
                )
            )

            _gameState.value = newState

            viewModelScope.launch {
                persistence.saveGameState(newState)
            }

            finishCurrentTurn()
            return
        }

        val finalWords = updatedWords.toMutableList().apply {
            this[nextWordIndex] = this[nextWordIndex].copy(
                state = WordState.IN_TURN
            )
        }

        val newState = currentState.copy(
            wordBulk = finalWords,
            current = currentState.current.copy(
                wordIndex = nextWordIndex,
                skippedWordIdsInTurn = skippedIds
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
            status = GameStatus.FINISHED
        )

        val maxScore = finalState.teams.maxOfOrNull { it.score } ?: 0
        val winners = finalState.teams.filter { it.score == maxScore }

        val record = GameRecord(
            id = finalState.gameId,
            finishedAt = System.currentTimeMillis(),
            winnerName = if (winners.size == 1) winners.first().name else null,
            isTie = winners.size > 1,
            scores = finalState.teams.map {
                TeamScore(
                    teamName = it.name,
                    score = it.score
                )
            },
            usedWords = finalState.wordBulk.map {
                UsedWordRecord(
                    id = it.id,
                    text = it.text
                )
            }
        )

        _records.value = listOf(record) + _records.value
        _gameState.value = finalState
        _screenState.value = ScreenState.FINAL
        stopTimer()

        viewModelScope.launch {
            val wordSetText = finalState.wordBulk.map { it.text }.toSet()
            persistence.removeActiveWords(wordSetText)
            persistence.saveGameState(finalState)

            persistence.saveGameRecord(record)
            _records.value = persistence.getGameRecords()
        }
        _screenState.value = ScreenState.FINAL
    }

    private fun startTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }

            finishCurrentTurn()
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

    /**
     * Words
     */
    fun showWordManagementScreen() {
        _screenState.value = ScreenState.WORD_MANAGEMENT
        searchWords("")
    }

    fun toggleShowDisabledWords() {
        _showDisabledWords.value = !_showDisabledWords.value
        searchWords()
    }

    fun searchWords(query: String = _wordSearchQuery.value) {
        _wordSearchQuery.value = query

        viewModelScope.launch {
            try {
                _wordList.value = wordRepository.searchWords(
                    query = query,
                    difficulties = _wordDifficultyFilter.value.toList(),
                    showDisabled = _showDisabledWords.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to search words: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun toggleWordDifficultyFilter(difficulty: String) {
        val current = _wordDifficultyFilter.value

        if (difficulty in current && current.size == 1) {
            return
        }

        _wordDifficultyFilter.value =
            if (difficulty in current) current - difficulty else current + difficulty

        searchWords()
    }

    fun setWordEnabled(wordId: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                wordRepository.setWordEnabled(wordId, enabled)
                searchWords(_wordSearchQuery.value)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to update word: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun enableAllWords() {
        viewModelScope.launch {
            try {
                val difficulties = _wordDifficultyFilter.value.toList()
                wordRepository.enableWordsByDifficulty(difficulties)
                searchWords(_wordSearchQuery.value)
                _errorMessage.value = "Enabled words for: ${difficulties.joinToString()}"
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to enable words: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun disableAllWords() {
        viewModelScope.launch {
            try {
                val difficulties = _wordDifficultyFilter.value.toList()
                wordRepository.disableWordsByDifficulty(difficulties)
                searchWords(_wordSearchQuery.value)
                _errorMessage.value = "Disabled words for: ${difficulties.joinToString()}. Press Enable All to restore."
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to disable words: ${e.message ?: "unknown error"}"
            }
        }
    }
}
