package com.example.partywordgame.models

import kotlinx.serialization.Serializable

@Serializable
data class GameRecord(
    val id: String,
    val finishedAt: Long,
    val winnerName: String?,
    val isTie: Boolean,
    val scores: List<TeamScore>
)

@Serializable
data class TeamScore(
    val teamName: String,
    val score: Int
)