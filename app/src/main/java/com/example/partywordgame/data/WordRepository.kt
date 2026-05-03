package com.example.partywordgame.data

import android.content.Context
import com.example.partywordgame.data.local.AppDatabase
import com.example.partywordgame.data.local.DictionaryMetaEntity
import com.example.partywordgame.data.local.WordEntity
import com.example.partywordgame.models.DictionaryFile
import com.example.partywordgame.models.GameMode
import com.example.partywordgame.models.GameSettings
import com.example.partywordgame.models.InsufficientWordsException
import com.example.partywordgame.models.Word
import kotlinx.serialization.json.Json

class WordRepository(
    private val context: Context
) {
    private val wordDao = AppDatabase.getInstance(context).wordDao()

    private var lastDictionaryMeta: DictionaryFile? = null

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val testWords = listOf(
        "самолёт", "корабль", "машина", "велосипед", "поезд",
        "стол", "стул", "кровать", "шкаф", "тумба",
        "яблоко", "банан", "апельсин", "груша", "виноград",
        "собака", "кошка", "лошадь", "корова", "свинья",
        "дом", "окно", "дверь", "крыша", "стена",
        "книга", "ручка", "карандаш", "тетрадь", "альбом",
        "вода", "огонь", "земля", "воздух", "небо",
        "часы", "телефон", "компьютер", "телевизор", "радио"
    )

    suspend fun getWordBulk(
        settings: GameSettings,
        activeWords: Set<String>,
        mode: GameMode
    ): List<Word> {
        return when (mode) {
            GameMode.TEST -> getTestWordBulk(settings, activeWords)
            GameMode.NORMAL -> getDatabaseWordBulk(settings, activeWords)
        }
    }

    private fun getTestWordBulk(
        settings: GameSettings,
        activeWords: Set<String>
    ): List<Word> {
        val availableWords = testWords
            .filter { it !in activeWords }
            .shuffled()
            .take(settings.bulkSize)

        if (availableWords.size < settings.bulkSize) {
            throw InsufficientWordsException(
                "Not enough words available. Requested: ${settings.bulkSize}, Available: ${availableWords.size}"
            )
        }

        return availableWords.mapIndexed { index, text ->
            Word(
                id = "test_word_$index",
                text = text
            )
        }
    }

    private suspend fun getDatabaseWordBulk(
        settings: GameSettings,
        activeWords: Set<String>
    ): List<Word> {
        val entities = wordDao.getRandomWords(
            limit = settings.bulkSize,
            difficulties = settings.difficulties,
            excludedWords = activeWords.toList()
        )

        if (entities.size < settings.bulkSize) {
            throw InsufficientWordsException(
                "Not enough words available. Requested: ${settings.bulkSize}, Available: ${entities.size}"
            )
        }

        return entities.map {
            Word(
                id = it.id,
                text = it.text
            )
        }
    }

    suspend fun importDefaultDictionaryFromAssets(): Int {
        val rawJson = context.assets
            .open("dictionaries/ru_default_words.json")
            .bufferedReader()
            .use { it.readText() }

        val dictionary = json.decodeFromString<DictionaryFile>(rawJson)

        lastDictionaryMeta = dictionary

        val entities = dictionary.words
            //.filter { it.enabled }
            .map {
                WordEntity(
                    id = it.id,
                    text = it.text,
                    language = dictionary.language,
                    difficulty = it.difficulty,
                    category = it.category,
                    enabled = it.enabled,
                    source = dictionary.dictionaryId
                )
            }

        val existingIds = wordDao.getAllWordIds().toSet()

        val newWords = entities.filter { it.id !in existingIds }
        val existingWords = entities.filter { it.id in existingIds }

        if (newWords.isNotEmpty()) {
            wordDao.insertWords(newWords)
        }

        existingWords.forEach { word ->
            wordDao.updateWordKeepEnabled(
                id = word.id,
                text = word.text,
                language = word.language,
                difficulty = word.difficulty,
                category = word.category,
                source = word.source
            )
        }

        return entities.size
    }

    suspend fun countDisabledWords(): Int {
        return wordDao.countDisabledWords()
    }

    suspend fun clearDictionary() {
        wordDao.clearWords()
    }

    suspend fun countAllWords(): Int {
        return wordDao.countAllWords()
    }

    suspend fun countEnabledWords(): Int {
        return wordDao.countEnabledWords()
    }

    suspend fun searchWords(
        query: String,
        difficulties: List<String>,
        showDisabled: Boolean
    ): List<WordEntity> {
        return wordDao.searchWordsByDifficulty(query, difficulties, showDisabled)
    }

    suspend fun setWordEnabled(wordId: String, enabled: Boolean) {
        wordDao.setWordEnabled(wordId, enabled)
    }

    suspend fun enableWordsByDifficulty(difficulties: List<String>) {
        wordDao.enableWordsByDifficulty(difficulties)
    }

    suspend fun disableWordsByDifficulty(difficulties: List<String>) {
        wordDao.disableWordsByDifficulty(difficulties)
    }

    fun getLastDictionaryMeta(): DictionaryFile? {
        return lastDictionaryMeta
    }

    suspend fun importDictionaryIfNeeded(): String {
        val rawJson = context.assets
            .open("dictionaries/ru_default_words.json")
            .bufferedReader()
            .use { it.readText() }

        val dictionary = json.decodeFromString<DictionaryFile>(rawJson)

        val metaDao = AppDatabase.getInstance(context).dictionaryMetaDao()
        val existing = metaDao.getMeta(dictionary.dictionaryId)

        if (existing != null && existing.version >= dictionary.version) {
            return "Dictionary is up to date (v${existing.version})"
        }

        val count = importDefaultDictionaryFromAssets()

        metaDao.upsert(
            DictionaryMetaEntity(
                id = dictionary.dictionaryId,
                version = dictionary.version,
                source = dictionary.source,
                license = dictionary.license
            )
        )

        return "Dictionary updated to v${dictionary.version}. Imported: $count words"
    }
}