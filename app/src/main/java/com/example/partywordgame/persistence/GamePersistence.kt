package com.example.partywordgame.persistence

import com.example.partywordgame.models.GameState

interface GamePersistence {
    /**
     * Saves the current game state
     */
    suspend fun saveGameState(gameState: GameState)
    
    /**
     * Loads the saved game state
     */
    suspend fun loadGameState(): GameState?
    
    /**
     * Clears the saved game state
     */
    suspend fun clearGameState()
    
    /**
     * Gets a list of words that are currently in use in active games
     * to prevent overlap between sessions
     */
    suspend fun getActiveWords(): Set<String>
    
    /**
     * Adds words from a new game to the active words set
     */
    suspend fun addActiveWords(words: Set<String>)
    
    /**
     * Removes words from active words when a game ends
     */
    suspend fun removeActiveWords(words: Set<String>)
}
