package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit,
    onResumeGameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecordsClick: () -> Unit,
    isResumeEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Party Word Game",
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onNewGameClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("New Game")
        }
        
        Button(
            onClick = onResumeGameClick,
            enabled = isResumeEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Resume Game")
        }

        Button(
            onClick = onRecordsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Records")
        }

        Button(
            onClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Settings")
        }
    }
}
