package com.example.partywordgame.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.partywordgame.R
import com.example.partywordgame.models.GameSettings
import com.example.partywordgame.models.GameMode

@Composable
fun SetupScreen(
    currentMode: GameMode,
    onSettingsConfirmed: (GameSettings) -> Unit,
    onBackClicked: () -> Unit
) {
    var bulkSize by remember { mutableStateOf(40) }
    var teamCount by remember { mutableStateOf(2) }
    var teamNames by remember {
        mutableStateOf(List(8) { index -> "Team ${index + 1}" })
    }
    var roundCount by remember { mutableStateOf(4) }
    var turnDuration by remember { mutableStateOf(60) }
    
    val bulkSizeOptions = listOf(40, 60, 80, 100)
    var selectedDifficulties by remember {
        mutableStateOf(setOf("easy", "medium", "hard"))
    }
    val isTestMode = currentMode == GameMode.TEST

    val screenHeight = LocalConfiguration.current.screenHeightDp

    val scale = when {
        screenHeight < 600 -> 0.75f
        screenHeight < 700 -> 0.85f
        else -> 1f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = if (isTestMode) "Testing Mode" else "Game Setup",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = (24 * scale).dp)
        )

        if (!isTestMode) {
            // Bulk Size Selector
            SettingRow(
                label = "Word Bulk Size",
                Modifier.padding(vertical = (6 * scale).dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                ) {
                    bulkSizeOptions.forEach { size ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { bulkSize = size }
                        ) {
                            RadioButton(
                                selected = bulkSize == size,
                                onClick = null
                            )
                            Text(
                                text = size.toString(),
                                modifier = Modifier.padding(start = (4 * scale).dp)
                            )
                        }
                    }
                }
            }

            SettingRow(
                label = "Word Difficulty",
                modifier = Modifier.padding(vertical = (6 * scale).dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((8 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("easy", "medium", "hard").forEach { difficulty ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                  if (difficulty in selectedDifficulties && selectedDifficulties.size == 1) {
                                    return@clickable
                                }

                                selectedDifficulties =
                                    if (difficulty in selectedDifficulties) {
                                        selectedDifficulties - difficulty
                                    } else {
                                        selectedDifficulties + difficulty
                                    }
                            }
                        ) {
                            Checkbox(
                                checked = difficulty in selectedDifficulties,
                                onCheckedChange = null
                            )

                            Text(
                                text = difficulty.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = (4 * scale).dp)
                            )
                        }
                    }
                }
            }

            // Team Count Selector
            SettingRow(
                label = "Number of Teams",
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (teamCount > 1) teamCount-- }) {
                        Text("-", style = MaterialTheme.typography.h6)
                    }
                    Text(
                        text = teamCount.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = { if (teamCount < 8) teamCount++ }) {
                        Text("+", style = MaterialTheme.typography.h6)
                    }
                }
            }

            SettingRow(
                label = "Teams names",
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Column {
                    repeat(teamCount) { index ->
                        OutlinedTextField(
                            value = teamNames[index],
                            onValueChange = { value ->
                                teamNames = teamNames.toMutableList().also {
                                    it[index] = value
                                }
                            },
                            label = { Text("Team ${index + 1} name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = (2 * scale).dp)
                        )
                    }
                }
            }

            // Round Count Selector
            SettingRow(
                label = "Number of Rounds",
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (roundCount > 1) roundCount-- }) {
                        Text("-", style = MaterialTheme.typography.h6)
                    }
                    Text(
                        text = roundCount.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = { if (roundCount < 6) roundCount++ }) {
                        Text("+", style = MaterialTheme.typography.h6)
                    }
                }
            }

            // Turn Duration Selector
            SettingRow(
                label = "Turn Duration (seconds)",
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (turnDuration >= 30) turnDuration -= 10 }) {
                        Text("-", style = MaterialTheme.typography.h6)
                    }
                    Text(
                        text = turnDuration.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = { if (turnDuration <= 180) turnDuration += 10 }) {
                        Text("+", style = MaterialTheme.typography.h6)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBackClicked,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = (8 * scale).dp)
            ) {
                Text(stringResource(R.string.back))
            }

            Button(
                onClick = {
                    val safeDifficulties = selectedDifficulties.ifEmpty {
                        setOf("easy", "medium", "hard")
                    }
                    val settings = if (isTestMode) {
                        GameSettings(
                            bulkSize = bulkSize,
                            teamCount = teamCount,
                            roundCount = roundCount,
                            turnDurationSeconds = turnDuration,
                            teamNames = teamNames.take(teamCount),
                            difficulties = safeDifficulties.toList()
                        )
                    } else {
                        GameSettings(
                            bulkSize = bulkSize,
                            teamCount = teamCount,
                            roundCount = roundCount,
                            turnDurationSeconds = turnDuration,
                            teamNames = teamNames.take(teamCount),
                        )
                    }

                    onSettingsConfirmed(settings)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = (8 * scale).dp)
            ) {
                Text(stringResource(R.string.start_game))
            }
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
