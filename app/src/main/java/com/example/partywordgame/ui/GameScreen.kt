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
    onPause: () -> Unit,
    onRestartRound: () -> Unit,
    onRestartGame: () -> Unit
) {
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
            val timerColor = if (timeLeft <= 10) {
                MaterialTheme.colors.error
            } else {
                MaterialTheme.colors.onBackground
            }
            Text(
                text = formatTime(timeLeft),
                color = timerColor,
                style = MaterialTheme.typography.h1,
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
            if (!isTimerRunning) {
                Button(
                    onClick = onStartTurn,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Start Turn")
                }
            } else {
                Text(
                    text = currentWord.text,
                    style = MaterialTheme.typography.h3,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Action buttons
        if (isTimerRunning && currentWord.state == WordState.IN_TURN) {
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
                        .padding(top = 46.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("Pause", color = MaterialTheme.colors.onError)
                }

                OutlinedButton(
                    onClick = onRestartRound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Restart Round")
                }

                OutlinedButton(
                    onClick = onRestartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Restart Game")
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
