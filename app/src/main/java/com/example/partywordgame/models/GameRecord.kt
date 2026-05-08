package com.example.partywordgame.models

import kotlinx.serialization.Serializable

@Serializable
data class GameRecord(
    val id: String,
    val finishedAt: Long,
    val winnerName: String?,
    val isTie: Boolean,
    val scores: List<TeamScore>,
    val usedWords: List<UsedWordRecord> = emptyList()
)

@Serializable
data class TeamScore(
    val teamName: String,
    val score: Int,
    val skippedCount: Int = 0,
    val netScore: Int = score - skippedCount
)

@Serializable
data class UsedWordRecord(
    val id: String,
    val text: String
)
