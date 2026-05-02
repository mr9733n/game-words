package com.example.partywordgame.data

import android.content.Context
import com.example.partywordgame.models.DictionaryFile
import com.example.partywordgame.models.GameMode
import com.example.partywordgame.models.GameSettings
import com.example.partywordgame.models.Word
import kotlinx.serialization.json.Json

class WordRepository(
    private val context: Context
) {
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

    private val assetWords by lazy {
        val rawJson = context.assets
            .open("dictionaries/ru_default_words.json")
            .bufferedReader()
            .use { it.readText() }

        json.decodeFromString<DictionaryFile>(rawJson)
            .words
            .filter { it.enabled }
    }

    fun getWordBulk(
        settings: GameSettings,
        activeWords: Set<String>,
        mode: GameMode = GameMode.TEST
    ): List<Word> {
        val sourceWords: List<Pair<String, String>> = when (mode) {
            GameMode.TEST -> testWords.mapIndexed { index, text ->
                "test_word_$index" to text
            }

            GameMode.NORMAL -> assetWords
                .filter { word ->
                    word.difficulty in settings.difficulties &&
                            (settings.categories.isEmpty() || word.category in settings.categories)
                }
                .map { word ->
                    word.id to word.text
                }
        }

        val availableWords = sourceWords
            .filter { (_, text) -> text !in activeWords }
            .shuffled()
            .take(settings.bulkSize)

        if (availableWords.size < settings.bulkSize) {
            throw IllegalStateException(
                "Not enough words available. Requested: ${settings.bulkSize}, Available: ${availableWords.size}"
            )
        }

        return availableWords.map { (id, text) ->
            Word(
                id = id,
                text = text
            )
        }
    }
}