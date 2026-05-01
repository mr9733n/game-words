package com.example.partywordgame.core

import com.example.partywordgame.models.*

class GameManager {
    private var gameState: GameState? = null
    
    /**
     * Starts a new game with the provided settings
     */
    fun startNewGame(settings: GameSettings, excludedWords: Set<String> = emptySet()): GameState {
        val dictionary = WordDictionary()
        val words = dictionary.selectWordBatch(settings.bulkSize, excludedWords)
        
        val teams = (1..settings.teamCount).map { 
            Team(id = "team_$it", score = 0) 
        }
        
        gameState = GameState(
            gameId = java.util.UUID.randomUUID().toString(),
            status = GameStatus.ACTIVE,
            settings = settings,
            teams = teams,
            wordBulk = words.map { it.copy(state = WordState.AVAILABLE) },
            current = CurrentTurn(round = 1, teamIndex = 0, wordIndex = 0)
        )
        
        return gameState!!
    }
    
    /**
     * Gets the current game state
     */
    fun getGameState(): GameState? = gameState
    
    /**
     * Marks the current word as guessed by the current team
     */
    fun markWordAsGuessed(): GameState {
        validateGameState()
        
        val currentState = gameState!!
        val currentWordIndex = currentState.current.wordIndex
        val currentTeamIndex = currentState.current.teamIndex
        
        // Update the word state to GUESSED
        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(state = WordState.GUESSED)
        }
        
        // Update team score
        val updatedTeams = currentState.teams.toMutableList().apply {
            this[currentTeamIndex] = this[currentTeamIndex].copy(score = this[currentTeamIndex].score + 1)
        }
        
        // Move to next word
        val nextWordIndex = findNextAvailableWordIndex(currentWordIndex, updatedWords)
        
        gameState = currentState.copy(
            teams = updatedTeams,
            wordBulk = updatedWords,
            current = currentState.current.copy(wordIndex = nextWordIndex)
        )
        
        return gameState!!
    }
    
    /**
     * Moves to the next team's turn without changing the word state
     */
    fun moveToNextTurn(): GameState {
        validateGameState()
        
        val currentState = gameState!!
        val nextTeamIndex = (currentState.current.teamIndex + 1) % currentState.settings.teamCount
        
        // If we're back to the first team, move to next word
        val nextWordIndex = if (nextTeamIndex == 0) {
            findNextAvailableWordIndex(currentState.current.wordIndex, currentState.wordBulk)
        } else {
            currentState.current.wordIndex
        }
        
        gameState = currentState.copy(
            current = currentState.current.copy(
                teamIndex = nextTeamIndex,
                wordIndex = nextWordIndex
            )
        )
        
        return gameState!!
    }
    
    /**
     * Moves to the next round, resetting word states to AVAILABLE
     */
    fun moveToNextRound(): GameState {
        validateGameState()
        
        val currentState = gameState!!
        val nextRound = currentState.current.round + 1
        
        // Reset all words to AVAILABLE state
        val resetWords = currentState.wordBulk.map { 
            it.copy(state = WordState.AVAILABLE) 
        }
        
        gameState = currentState.copy(
            wordBulk = resetWords,
            current = currentState.current.copy(
                round = nextRound,
                teamIndex = 0,
                wordIndex = 0
            )
        )
        
        return gameState!!
    }
    
    /**
     * Checks if all words have been guessed in the current round
     */
    fun areAllWordsGuessed(): Boolean {
        validateGameState()
        return gameState!!.wordBulk.all { it.state == WordState.GUESSED }
    }
    
    /**
     * Checks if the game has reached its final round
     */
    fun isFinalRound(): Boolean {
        validateGameState()
        return gameState!!.current.round >= gameState!!.settings.roundCount
    }
    
    /**
     * Finishes the game and sets its status to FINISHED
     */
    fun finishGame(): GameState {
        validateGameState()
        
        gameState = gameState!!.copy(status = GameStatus.FINISHED)
        return gameState!!
    }
    
    /**
     * Sets a word to IN_TURN state (when a turn starts)
     */
    fun startTurn(): GameState {
        validateGameState()
        
        val currentState = gameState!!
        val currentWordIndex = currentState.current.wordIndex
        
        val updatedWords = currentState.wordBulk.toMutableList().apply {
            this[currentWordIndex] = this[currentWordIndex].copy(state = WordState.IN_TURN)
        }
        
        gameState = currentState.copy(wordBulk = updatedWords)
        return gameState!!
    }
    
    /**
     * Finds the next available word that hasn't been guessed yet
     */
    private fun findNextAvailableWordIndex(startingIndex: Int, words: List<Word>): Int {
        // Try to find the next non-guessed word after the current index
        for (i in startingIndex + 1 until words.size) {
            if (words[i].state != WordState.GUESSED) {
                return i
            }
        }
        
        // If not found, loop back to the beginning
        for (i in 0 until startingIndex) {
            if (words[i].state != WordState.GUESSED) {
                return i
            }
        }
        
        // If all words are guessed, return the same index (shouldn't happen in normal flow)
        return startingIndex
    }
    
    /**
     * Validates that game state exists
     */
    private fun validateGameState() {
        if (gameState == null) {
            throw IllegalStateException("Game state is not initialized. Start a new game first.")
        }
    }
}
