package com.example.partywordgame.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["language", "text"], unique = true)
    ]
)
data class WordEntity(
    @PrimaryKey val id: String,
    val text: String,
    val language: String,
    val difficulty: String,
    val category: String,
    val enabled: Boolean,
    val source: String
)

@Entity(tableName = "dictionary_meta")
data class DictionaryMetaEntity(
    @PrimaryKey val id: String,
    val version: Int,
    val source: String?,
    val license: String?
)