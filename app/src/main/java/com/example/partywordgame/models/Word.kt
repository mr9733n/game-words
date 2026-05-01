package com.example.partywordgame.models

data class Word(
    val id: String,
    val text: String,
    val state: WordState = WordState.AVAILABLE
)

enum class WordState {
    AVAILABLE,
    IN_TURN,
    GUESSED,
}
