package com.example.partywordgame.models

import androidx.compose.ui.graphics.Color

data class Team(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val score: Int = 0,
    val roundScore: Int = 0,
    val skippedCount: Int = 0,
    val roundSkippedCount: Int = 0
)

val teamColors = listOf(
    Color(0xFFEF5350),
    Color(0xFF42A5F5),
    Color(0xFFFFCA28),
    Color(0xFF66BB6A),
    Color(0xFFAB47BC),
    Color(0xFFFF7043),
    Color(0xFF26C6DA),
    Color(0xFF8D6E63)
)
