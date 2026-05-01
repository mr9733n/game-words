package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.partywordgame.models.GameSettings

@Composable
fun SetupScreen(
    onSettingsConfirmed: (GameSettings) -> Unit,
    onBackClicked: () -> Unit
) {
    var bulkSize by remember { mutableStateOf(60) }
    var teamCount by remember { mutableStateOf(2) }
    var roundCount by remember { mutableStateOf(4) }
    var turnDuration by remember { mutableStateOf(60) }
    
    val bulkSizeOptions = listOf(40, 60, 80, 100)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Game Setup",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Bulk Size Selector
        SettingRow(
            label = "Word Bulk Size",
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row {
                bulkSizeOptions.forEach { size ->
                    RadioButton(
                        selected = bulkSize == size,
                        onClick = { bulkSize = size }
                    )
                    Text(text = size.toString(), modifier = Modifier.padding(end = 16.dp))
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
                IconButton(onClick = { if (teamCount > 2) teamCount-- }) {
                    Text("-", style = MaterialTheme.typography.h6)
                }
                Text(
                    text = teamCount.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.h6
                )
                IconButton(onClick = { if (teamCount < 6) teamCount++ }) {
                    Text("+", style = MaterialTheme.typography.h6)
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
                IconButton(onClick = { if (turnDuration > 30) turnDuration -= 10 }) {
                    Text("-", style = MaterialTheme.typography.h6)
                }
                Text(
                    text = turnDuration.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.h6
                )
                IconButton(onClick = { if (turnDuration < 120) turnDuration += 10 }) {
                    Text("+", style = MaterialTheme.typography.h6)
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
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = {
                    onSettingsConfirmed(
                        GameSettings(
                            bulkSize = bulkSize,
                            teamCount = teamCount,
                            roundCount = roundCount,
                            turnDurationSeconds = turnDuration
                        )
                    )
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Start Game")
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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}
