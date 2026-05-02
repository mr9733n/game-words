package com.example.partywordgame.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: String,
    val text: String,
    val language: String,
    val difficulty: String,
    val category: String,
    val enabled: Boolean,
    val source: String
)