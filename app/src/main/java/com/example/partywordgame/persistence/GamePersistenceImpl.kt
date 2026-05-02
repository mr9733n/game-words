package com.example.partywordgame.persistence

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.partywordgame.models.GameState
import com.example.partywordgame.models.GameRecord
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        private val GAME_RECORDS_KEY = stringPreferencesKey("game_records")
        private val TAG = "GamePersistenceImpl"
    }
    
    override suspend fun saveGameState(gameState: GameState) {
        try {
            val gameStateJson = json.encodeToString(gameState)
            context.dataStore.edit { preferences ->
                preferences[GAME_STATE_KEY] = gameStateJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game state", e)
        }
    }
    
    override suspend fun loadGameState(): GameState? {
        return try {
            val preferences = context.dataStore.data.first()
            val gameStateJson = preferences[GAME_STATE_KEY] ?: return null
            json.decodeFromString<GameState>(gameStateJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game state", e)
            null
        }
    }
    
    override suspend fun clearGameState() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(GAME_STATE_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing game state", e)
        }
    }
    
    override suspend fun getActiveWords(): Set<String> {
        return try {
            val preferences = context.dataStore.data.first()
            val activeWordsJson = preferences[ACTIVE_WORDS_KEY] ?: return emptySet()
            json.decodeFromString<Set<String>>(activeWordsJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active words", e)
            emptySet()
        }
    }
    
    override suspend fun addActiveWords(words: Set<String>) {
        try {
            val currentActiveWords = getActiveWords()
            val updatedWords = currentActiveWords + words
            val updatedWordsJson = json.encodeToString(updatedWords)
            
            context.dataStore.edit { preferences ->
                preferences[ACTIVE_WORDS_KEY] = updatedWordsJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding active words", e)
        }
    }
    
    override suspend fun removeActiveWords(words: Set<String>) {
        try {
            val currentActiveWords = getActiveWords()
            val updatedWords = currentActiveWords - words
            val updatedWordsJson = json.encodeToString(updatedWords)
            
            context.dataStore.edit { preferences ->
                preferences[ACTIVE_WORDS_KEY] = updatedWordsJson
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing active words", e)
        }
    }

    override suspend fun saveGameRecord(record: GameRecord) {
        val records = getGameRecords()
        val updatedRecords = listOf(record) + records

        context.dataStore.edit { preferences ->
            preferences[GAME_RECORDS_KEY] = json.encodeToString(updatedRecords)
        }
    }

    override suspend fun getGameRecords(): List<GameRecord> {
        return try {
            val preferences = context.dataStore.data.first()
            val recordsJson = preferences[GAME_RECORDS_KEY] ?: return emptyList()
            json.decodeFromString<List<GameRecord>>(recordsJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting game records", e)
            emptyList()
        }
    }

    override suspend fun clearGameRecords() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(GAME_RECORDS_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing game records", e)
        }
    }
}
