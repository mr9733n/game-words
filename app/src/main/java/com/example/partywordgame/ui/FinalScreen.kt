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
fun FinalScreen(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val maxScore = gameState.teams.maxOfOrNull { it.score - it.skippedCount } ?: 0
    val winners = gameState.teams.withIndex().filter { it.value.score - it.value.skippedCount == maxScore }
    val isTie = winners.size > 1
    val winningTeamIndex = winners.firstOrNull()?.index
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Text(
            text = if (isTie) "It's a Tie!" else "Game Over!",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Winner section
        if (!isTie && winningTeamIndex != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = 8.dp,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Winner:",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = gameState.teams[winningTeamIndex].name,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
        
        // Final scores
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
                    text = "Final Scores",
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
                        Text(
                            text = team.name + if (winningTeamIndex == index && !isTie) " (Winner)" else "",
                            fontWeight = if (winningTeamIndex == index && !isTie) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${team.score - team.skippedCount} (${team.score} - ${team.skippedCount})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Play Again")
            }
            
            Button(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors()
            ) {
                Text("Exit")
            }
        }
    }
}
