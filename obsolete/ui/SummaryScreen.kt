package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.partywordgame.models.GameState

@Composable
fun SummaryScreen(
    gameState: GameState,
    onContinue: () -> Unit,
    onFinishGame: () -> Unit
) {
    val isLastRound = gameState.current.round >= gameState.settings.roundCount
    val areAllWordsGuessed = gameState.wordBulk.all { it.state == com.example.partywordgame.models.WordState.GUESSED }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Text(
            text = if (isLastRound && areAllWordsGuessed) "Game Completed!" else "Round ${gameState.current.round} Summary",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Scores section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Team Scores",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                gameState.teams.forEachIndexed { index, team ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Team ${index + 1}")
                        Text(team.score.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Round info
        if (!isLastRound && areAllWordsGuessed) {
            Text(
                text = "All words guessed! Ready for next round?",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else if (!areAllWordsGuessed) {
            Text(
                text = "Round not completed yet. Continue playing to finish all words.",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLastRound && areAllWordsGuessed) {
                Button(
                    onClick = onFinishGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Finish Game")
                }
            } else if (areAllWordsGuessed) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Next Round")
                }
            } else {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Continue Playing")
                }
            }
        }
    }
}
