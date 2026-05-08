package com.example.partywordgame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import com.example.partywordgame.models.teamColors
import com.example.partywordgame.audio.SoundPlayer
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
    onRestartGame: () -> Unit,
    maxSkipsPerTurn: Int
) {
    val currentWord = gameState.wordBulk[gameState.current.wordIndex]
    val currentTeam = gameState.teams[gameState.current.teamIndex]
    val currentTeamColor = teamColors[currentTeam.colorIndex % teamColors.size]
    val skipsUsed = gameState.current.skipCountInTurn
    val canSkip = skipsUsed < maxSkipsPerTurn
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
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with game info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${gameState.current.round}",
                style = MaterialTheme.typography.h3.copy(
                    fontSize = MaterialTheme.typography.h3.fontSize * scale
                )
            )

            Text(
                text = currentTeam.name,
                style = MaterialTheme.typography.h4.copy(
                    fontSize = MaterialTheme.typography.h4.fontSize * scale
                ),
                fontWeight = FontWeight.Bold,
                color = currentTeamColor,
                modifier = Modifier.padding(top = (4 * scale).dp)
            )
            
            Text(
                text = getRoundRule(gameState.current.round),
                style = MaterialTheme.typography.h5.copy(
                    fontSize = MaterialTheme.typography.h5.fontSize * scale
                ),
                fontSize = (16 * scale).sp,
                modifier = Modifier.padding(top = (4 * scale).dp)
            )

            // Timer display
            val context = LocalContext.current
            val soundPlayer = remember { SoundPlayer(context) }

            LaunchedEffect(timeLeft) {
                if (timeLeft == 1) {
                    soundPlayer.playNotificationSound()
                }
            }

            val timerColor = if (timeLeft <= 10) {
                MaterialTheme.colors.error
            } else {
                MaterialTheme.colors.onBackground
            }

            val timerText = if (timeLeft <= 10) {
                "⏰ ${formatTime(timeLeft)}"
            } else {
                formatTime(timeLeft)
            }

            Text(
                text = timerText,
                color = timerColor,
                style = MaterialTheme.typography.h2.copy(
                    fontSize = MaterialTheme.typography.h2.fontSize * scale
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = (16 * scale).dp)
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
                    modifier = Modifier.padding(bottom = (16 * scale).dp)
                ) {
                    Text("Start Turn")
                }
            } else {
                Text(
                    text = currentWord.text,
                    style = MaterialTheme.typography.h4.copy(
                        fontSize = MaterialTheme.typography.h4.fontSize * scale
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action buttons
        if (isTimerRunning && currentWord.state == WordState.IN_TURN) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (16 * scale).dp),
                verticalArrangement = Arrangement.spacedBy((8 * scale).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                ) {
                    Button(
                        onClick = onGuessed,
                        modifier = Modifier
                            .weight(1f)
                            .height((48 * scale).dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Guessed", color = MaterialTheme.colors.onPrimary)
                    }

                    Button(
                        onClick = onNotGuessed,
                        modifier = Modifier
                            .weight(1f)
                            .height((48 * scale).dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                    ) {
                        Text("Missed", color = MaterialTheme.colors.onSecondary)
                    }

                    Button(
                        onClick = onNextWord,
                        enabled = canSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height((48 * scale).dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Text("Skip $skipsUsed/$maxSkipsPerTurn", color = MaterialTheme.colors.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height((24 * scale).dp))

                Button(
                    onClick = onPause,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((42 * scale).dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("Pause", color = MaterialTheme.colors.onError)
                }

                Spacer(modifier = Modifier.height((8 * scale).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                ) {
                    OutlinedButton(
                        onClick = onRestartRound,
                        modifier = Modifier
                            .weight(1f)
                            .height((42 * scale).dp)
                    ) {
                        Text("Restart Round")
                    }

                    OutlinedButton(
                        onClick = onRestartGame,
                        modifier = Modifier
                            .weight(1f)
                            .height((42 * scale).dp)
                    ) {
                        Text("New Game")
                    }
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
