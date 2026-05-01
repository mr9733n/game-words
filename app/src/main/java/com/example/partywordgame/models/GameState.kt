package com.example.partywordgame.models

import java.util.UUID

data class GameState(
    val gameId: String = UUID.randomUUID().toString(),
    val status: GameStatus = GameStatus.SETUP,
    val settings: GameSettings,
    val teams: List<Team>,
    val wordBulk: List<Word>,
    val current: CurrentTurn
)

data class CurrentTurn(
    val round: Int,
    val teamIndex: Int,
    val wordIndex: Int,
    val skippedWordIdsInTurn: Set<String> = emptySet()
)

enum class GameStatus {
    SETUP,
    ACTIVE,
    FINISHED
}

enum class GameMode {
    TEST,
    NORMAL
}