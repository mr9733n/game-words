package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.text.style.TextOverflow
import com.example.partywordgame.data.local.WordEntity

@Composable
fun WordManagementScreen(
    query: String,
    words: List<WordEntity>,
    selectedDifficulties: Set<String>,
    onQueryChanged: (String) -> Unit,
    onDifficultyClicked: (String) -> Unit,
    onWordEnabledChanged: (String, Boolean) -> Unit,
    onEnableAllClicked: () -> Unit,
    onDisableAllClicked: () -> Unit,
    onBackClicked: () -> Unit,
    showDisabledWords: Boolean,
    onToggleShowDisabledWords: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Word Management",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text("Search words") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("easy", "medium", "hard").forEach { difficulty ->
                Button(
                    onClick = { onDifficultyClicked(difficulty) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (difficulty in selectedDifficulties) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.surface
                        }
                    )
                ) {
                    Text(
                        text = difficulty.replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleShowDisabledWords,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (showDisabledWords) "Hide Disabled" else "Show Disabled",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onEnableAllClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Enable All")
            }

            Button(
                onClick = onDisableAllClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Disable All")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBackClicked,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Words: ${words.size}",
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(words, key = { it.id }) { word ->
                WordRow(
                    word = word,
                    onEnabledChanged = { enabled ->
                        onWordEnabledChanged(word.id, enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun WordRow(
    word: WordEntity,
    onEnabledChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = word.enabled,
                onCheckedChange = onEnabledChanged
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = word.text,
                    style = MaterialTheme.typography.h6
                )

                Text(
                    text = "${word.difficulty} · ${word.category}",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}