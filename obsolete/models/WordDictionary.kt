package com.example.partywordgame.models

class WordDictionary {
    // In a real implementation, this would be loaded from a database or file
    private val allWords = listOf(
        "самолёт", "корабль", "машина", "велосипед", "поезд",
        "стол", "стул", "кровать", "шкаф", "тумба",
        "яблоко", "банан", "апельсин", "груша", "виноград",
        "собака", "кошка", "лошадь", "корова", "свинья",
        "дом", "окно", "дверь", "крыша", "стена",
        "книга", "ручка", "карандаш", "тетрадь", "альбом",
        "вода", "огонь", "земля", "воздух", "небо",
        "часы", "телефон", "компьютер", "телевизор", "радио"
    )

    /**
     * Selects a random batch of words for the game
     *
     * @param count Number of words to select
     * @param excludedWords Words that should not be included (from active sessions)
     * @return List of selected words
     */
    fun selectWordBatch(count: Int, excludedWords: Set<String> = emptySet()): List<Word> {
        // Filter out excluded words
        val availableWords = allWords.filter { it !in excludedWords }
        
        // Check if we have enough words
        if (availableWords.size < count) {
            throw InsufficientWordsException(
                "Not enough words available. Requested: $count, Available: ${availableWords.size}"
            )
        }
        
        // Randomly shuffle and take the required count
        return availableWords.shuffled().take(count).mapIndexed { index, wordText ->
            Word(id = "word_$index", text = wordText)
        }
    }
    
    /**
     * Returns total count of all available words
     */
    fun getTotalWordCount(): Int = allWords.size
    
    /**
     * Returns count of words excluding the specified ones
     */
    fun getAvailableWordCount(excludedWords: Set<String> = emptySet()): Int {
        return allWords.count { it !in excludedWords }
    }
}

class InsufficientWordsException(message: String) : Exception(message)
