package com.example.partywordgame.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {
    @Query("""
        SELECT * FROM words
        WHERE enabled = 1
        AND difficulty IN (:difficulties)
        AND text NOT IN (:excludedWords)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomWords(
        limit: Int,
        difficulties: List<String>,
        excludedWords: List<String>
    ): List<WordEntity>

    @Query("""
        SELECT * FROM words
        WHERE text LIKE '%' || :query || '%'
        ORDER BY text ASC
        LIMIT 200
    """)
    suspend fun searchWords(query: String): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("UPDATE words SET enabled = :enabled WHERE id = :wordId")
    suspend fun setWordEnabled(wordId: String, enabled: Boolean)

    @Query("UPDATE words SET enabled = 1")
    suspend fun enableAllWords()

    @Query("DELETE FROM words")
    suspend fun clearWords()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun countAllWords(): Int

    @Query("SELECT COUNT(*) FROM words WHERE enabled = 1")
    suspend fun countEnabledWords(): Int
}