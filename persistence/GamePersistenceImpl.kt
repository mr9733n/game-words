package com.example.partywordgame.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.partywordgame.models.GameState
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class GamePersistenceImpl(private val context: Context) : GamePersistence {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    companion object {
        private val GAME_STATE_KEY = stringPreferencesKey("game_state")
        private val ACTIVE_WORDS_KEY = stringPreferencesKey("active_words")
    }
    
    override suspend fun saveGameState(gameState: GameState) {
        val gameStateJson = json.encodeToString(gameState)
        context.dataStore.edit { preferences ->
            preferences[GAME_STATE_KEY] = gameStateJson
        }
    }
    
    override suspend fun loadGameState(): GameState? {
        val preferences = context.dataStore.data.first()
        val gameStateJson = preferences[GAME_STATE_KEY] ?: return null
        return try {
            json.decodeFromString<GameState>(gameStateJson)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun clearGameState() {
        context.dataStore.edit { preferences ->
            preferences.remove(GAME_STATE_KEY)
        }
    }
    
    override suspend fun getActiveWords(): Set<String> {
        val preferences = context.dataStore.data.first()
        val activeWordsJson = preferences[ACTIVE_WORDS_KEY] ?: return emptySet()
        return try {
            json.decodeFromString<Set<String>>(activeWordsJson)
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun addActiveWords(words: Set<String>) {
        val currentActiveWords = getActiveWords()
        val updatedWords = currentActiveWords + words
        val updatedWordsJson = json.encodeToString(updatedWords)
        
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_WORDS_KEY] = updatedWordsJson
        }
    }
    
    override suspend fun removeActiveWords(words: Set<String>) {
        val currentActiveWords = getActiveWords()
        val updatedWords = currentActiveWords - words
        val updatedWordsJson = json.encodeToString(updatedWords)
        
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_WORDS_KEY] = updatedWordsJson
        }
    }
}
