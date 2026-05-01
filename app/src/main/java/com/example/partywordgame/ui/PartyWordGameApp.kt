package com.example.partywordgame.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.partywordgame.viewmodel.GameViewModel
import com.example.partywordgame.viewmodel.ScreenState

@Composable
fun PartyWordGameApp(
    viewModel: GameViewModel = viewModel()
) {
    val screenState = viewModel.screenState.collectAsState().value
    val gameState = viewModel.gameState.collectAsState().value
    val timeLeft = viewModel.timeLeft.collectAsState().value
    val isTimerRunning = viewModel.isTimerRunning.collectAsState().value

    when (screenState) {
        ScreenState.HOME -> HomeScreen(
            onNewGameClick = { viewModel.showSetupScreen() },
            onResumeGameClick = { viewModel.resumeGame() },
            onSettingsClick = { viewModel.showSettingsScreen() },
            isResumeEnabled = viewModel.canResumeGame()
        )

        ScreenState.SETTINGS -> SettingsScreen(
            onBackClicked = { viewModel.showHomeScreen() },
            onResetStateClicked = { viewModel.resetSavedState() },
            onClearActiveWordsClicked = { viewModel.clearActiveWords() },
            onTestModeClicked = { viewModel.setTestMode() },
            onGameModeClicked = { viewModel.setGameMode() }
        )

        ScreenState.SETUP -> SetupScreen(
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
}