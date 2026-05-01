package com.example.partywordgame.models

data class GameSettings(
    val bulkSize: Int,
    val teamCount: Int,
    val roundCount: Int = 4,
    val turnDurationSeconds: Int = 60
)
