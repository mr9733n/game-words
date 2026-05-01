package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.partywordgame.models.GameState
import com.example.partywordgame.models.WordState

@Composable
fun GameScreen(
    gameState: GameState,
    timeLeft: Int,
    isTimerRunning: Boolean,
    onStartTurn: () -> Unit,
    onGuessed: () -> Unit,
    onNotGuessed: () -> Unit,
    onNextWord: () -> Unit,
    onPause: () -> Unit
) {
    val currentTeam = gameState.teams[gameState.current.teamIndex]
    val currentWord = gameState.wordBulk[gameState.current.wordIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with game info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${gameState.current.round}",
                style = MaterialTheme.typography.h6
            )
            
            Text(
                text = "Team ${gameState.current.teamIndex + 1}",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = getRoundRule(gameState.current.round),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Timer display
            Text(
                text = formatTime(timeLeft),
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        
        // Main content area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Show either the word or a start button
            when (currentWord.state) {
                WordState.IN_TURN -> {
                    Text(
                        text = currentWord.text,
                        style = MaterialTheme.typography.h3,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                WordState.GUESSED -> {
                    Text(
                        text = currentWord.text,
                        style = MaterialTheme.typography.h3,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }
                WordState.AVAILABLE -> {
                    Button(
                        onClick = onStartTurn,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Start Turn")
                    }
                }
            }
        }
        
        // Action buttons
        if (currentWord.state == WordState.IN_TURN) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onGuessed,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Guessed", color = MaterialTheme.colors.onPrimary)
                    }
                    
                    Button(
                        onClick = onNotGuessed,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                    ) {
                        Text("Not Guessed", color = MaterialTheme.colors.onSecondary)
                    }
                    
                    Button(
                        onClick = onNextWord,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Text("Skip", color = MaterialTheme.colors.onSurface)
                    }
                }
                
                Button(
                    onClick = onPause,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("Pause", color = MaterialTheme.colors.onError)
                }
            }
        }
    }
}

private fun getRoundRule(round: Int): String {
    return when (round) {
        1 -> "Explanation Round - No related words"
        2 -> "Charades Round - Only gestures"
        3 -> "One Word Round - Single hint only"
        4 -> "Drawing Round - Draw offline"
        else -> "Regular Round"
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
