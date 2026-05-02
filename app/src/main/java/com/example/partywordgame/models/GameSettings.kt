package com.example.partywordgame.models

data class GameSettings(
    val bulkSize: Int,
    val teamCount: Int,
    val roundCount: Int,
    val turnDurationSeconds: Int,
    val teamNames: List<String> = emptyList(),
    val difficulties: List<String> = listOf("easy", "medium", "hard"),
    val categories: List<String> = emptyList()
)
