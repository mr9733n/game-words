package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit,
    onResetStateClicked: () -> Unit,
    onClearActiveWordsClicked: () -> Unit,
    onTestModeClicked: () -> Unit,
    onGameModeClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onResetStateClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Saved Game")
            }

            Button(
                onClick = onClearActiveWordsClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Active Words")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mode",
                style = MaterialTheme.typography.h6
            )

            Button(
                onClick = onTestModeClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Mode")
            }

            Button(
                onClick = onGameModeClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Game Mode")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}