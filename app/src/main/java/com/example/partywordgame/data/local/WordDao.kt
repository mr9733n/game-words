package com.example.partywordgame.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface DictionaryMetaDao {

    @Query("SELECT * FROM dictionary_meta WHERE id = :id LIMIT 1")
    suspend fun getMeta(id: String): DictionaryMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: DictionaryMetaEntity)
}

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
    AND difficulty IN (:difficulties)
    AND (:showDisabled = 1 OR enabled = 1)
    ORDER BY text ASC
    LIMIT 1500
""")
    suspend fun searchWordsByDifficulty(
        query: String,
        difficulties: List<String>,
        showDisabled: Boolean
    ): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("UPDATE words SET enabled = :enabled WHERE id = :wordId")
    suspend fun setWordEnabled(wordId: String, enabled: Boolean)

    @Query("UPDATE words SET enabled = 1")
    suspend fun enableAllWords()

    @Query("UPDATE words SET enabled = 0")
    suspend fun disableAllWords()

    @Query("UPDATE words SET enabled = 1 WHERE difficulty IN (:difficulties)")
    suspend fun enableWordsByDifficulty(difficulties: List<String>)

    @Query("UPDATE words SET enabled = 0 WHERE difficulty IN (:difficulties)")
    suspend fun disableWordsByDifficulty(difficulties: List<String>)

    @Query("DELETE FROM words")
    suspend fun clearWords()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun countAllWords(): Int

    @Query("SELECT COUNT(*) FROM words WHERE enabled = 1")
    suspend fun countEnabledWords(): Int

    @Query("SELECT id FROM words")
    suspend fun getAllWordIds(): List<String>

    @Query("""
    UPDATE words
    SET text = :text,
        language = :language,
        difficulty = :difficulty,
        category = :category,
        source = :source
    WHERE id = :id
""")
    suspend fun updateWordKeepEnabled(
        id: String,
        text: String,
        language: String,
        difficulty: String,
        category: String,
        source: String
    )
}