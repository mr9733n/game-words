package com.example.partywordgame.models

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryFile(
    val dictionaryId: String,
    val language: String,
    val version: Int,
    val source: String? = null,
    val license: String? = null,
    val words: List<DictionaryWord>
)

@Serializable
data class DictionaryWord(
    val id: String,
    val text: String,
    val difficulty: String = "medium",
    val category: String = "general",
    val enabled: Boolean = true
)