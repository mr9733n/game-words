package com.example.partywordgame.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import com.example.partywordgame.viewmodel.GameViewModel
import com.example.partywordgame.viewmodel.ScreenState

@Composable
fun PartyWordGameApp(
    viewModel: GameViewModel = viewModel()
) {
    val errorMessage = viewModel.errorMessage.collectAsState().value
    val screenState = viewModel.screenState.collectAsState().value
    val gameState = viewModel.gameState.collectAsState().value
    val timeLeft = viewModel.timeLeft.collectAsState().value
    val isTimerRunning = viewModel.isTimerRunning.collectAsState().value
    val currentMode = viewModel.currentMode.collectAsState().value
    val records = viewModel.records.collectAsState().value

    when (screenState) {
        ScreenState.HOME -> HomeScreen(
            onNewGameClick = { viewModel.showSetupScreen() },
            onResumeGameClick = { viewModel.resumeGame() },
            onSettingsClick = { viewModel.showSettingsScreen() },
            onRecordsClick = { viewModel.showRecordsScreen() },
            isResumeEnabled = viewModel.canResumeGame()
        )

        ScreenState.RECORDS -> RecordsScreen(
            records = records,
            onBackClicked = { viewModel.showHomeScreen() }
        )

        ScreenState.SETTINGS -> SettingsScreen(
            currentMode = currentMode,
            onBackClicked = { viewModel.showHomeScreen() },
            onResetStateClicked = { viewModel.resetSavedState() },
            onClearActiveWordsClicked = { viewModel.clearActiveWords() },
            onTestModeClicked = { viewModel.setTestMode() },
            onGameModeClicked = { viewModel.setGameMode() }
        )

        ScreenState.SETUP -> SetupScreen(
            currentMode = currentMode,
            onSettingsConfirmed = { viewModel.startNewGame(it) },
            onBackClicked = { viewModel.showHomeScreen() }
        )

        ScreenState.GAME -> {
            if (gameState != null) {
                GameScreen(
                    gameState = gameState,
                    timeLeft = timeLeft,
                    isTimerRunning = isTimerRunning,
                    onStartTurn = { viewModel.startTurn() },
                    onGuessed = { viewModel.markWordAsGuessed() },
                    onNotGuessed = { viewModel.skipWord() },
                    onNextWord = { viewModel.skipWord() },
                    onPause = { viewModel.pauseGame() },
                    onRestartRound = { viewModel.restartRound() },
                    onRestartGame = { viewModel.restartGame() }
                )
            }
        }

        ScreenState.SUMMARY -> {
            if (gameState != null) {
                SummaryScreen(
                    gameState = gameState,
                    onContinue = { viewModel.continueGame() },
                    onFinishGame = { viewModel.finishGame() }
                )
            }
        }

        ScreenState.FINAL -> {
            if (gameState != null) {
                FinalScreen(
                    gameState = gameState,
                    onPlayAgain = { viewModel.showSetupScreen() },
                    onExit = { viewModel.showHomeScreen() }
                )
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Cannot start game") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}