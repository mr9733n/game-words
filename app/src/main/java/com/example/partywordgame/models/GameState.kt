package com.example.partywordgame.models

import kotlinx.serialization.Serializable
import java.util.UUID

data class GameState(
    val gameId: String = UUID.randomUUID().toString(),
    val status: GameStatus = GameStatus.SETUP,
    val settings: GameSettings,
    val teams: List<Team>,
    val wordBulk: List<Word>,
    val current: CurrentTurn
)

@Serializable
data class CurrentTurn(
    val round: Int,
    val teamIndex: Int,
    val wordIndex: Int,
    val skippedWordIdsInTurn: List<String> = emptyList(),
    val skipCountInTurn: Int = 0
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
