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
import androidx.compose.material.ButtonDefaults
import com.example.partywordgame.models.GameMode

@Composable
fun SettingsScreen(
    currentMode: GameMode,
    onBackClicked: () -> Unit,
    onResetStateClicked: () -> Unit,
    onClearActiveWordsClicked: () -> Unit,
    onTestModeClicked: () -> Unit,
    onGameModeClicked: () -> Unit,
    onImportDictionaryClicked: () -> Unit,
    onDictionaryStatsClicked: () -> Unit,
    onWordManagementClicked: () -> Unit,
    onClearDictionaryClicked: () -> Unit
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mode",
                style = MaterialTheme.typography.h6
            )

            Button(
                onClick = onTestModeClicked,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (currentMode == GameMode.TEST)
                        MaterialTheme.colors.primary
                    else
                        MaterialTheme.colors.surface
                )
            ) {
                Text("Test Mode")
            }

            Button(
                onClick = onGameModeClicked,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (currentMode == GameMode.NORMAL)
                        MaterialTheme.colors.primary
                    else
                        MaterialTheme.colors.surface
                )
            ) {
                Text("Game Mode")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Words Dictionary",
                style = MaterialTheme.typography.h6
            )

            Button(
                onClick = onImportDictionaryClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Dictionary")
            }

            Button(
                onClick = onDictionaryStatsClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dictionary Stats")
            }

            Button(
                onClick = onClearDictionaryClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Dictionary")
            }

            Button(
                onClick = onWordManagementClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Word Management")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onBackClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}