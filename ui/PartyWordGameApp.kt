package com.example.partywordgame.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.partywordgame.viewmodel.GameViewModel

@Composable
fun PartyWordGameApp(
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    
    when (screenState) {
        ScreenState.HOME -> HomeScreen(
            onNewGameClick = { viewModel.showSetupScreen() },
            onResumeGameClick = { viewModel.resumeGame() },
            isResumeEnabled = viewModel.canResumeGame()
        )
        
        ScreenState.SETUP -> SetupScreen(
            onSettingsConfirmed = { settings -> viewModel.startNewGame(settings) },
            onBackClicked = { viewModel.showHomeScreen() }
        )
        
        ScreenState.GAME -> {
            if (gameState != null) {
                GameScreen(
                    gameState = gameState!!,
                    timeLeft = timeLeft,
                    isTimerRunning = isTimerRunning,
                    onStartTurn = { viewModel.startTurn() },
                    onGuessed = { viewModel.markWordAsGuessed() },
                    onNotGuessed = { viewModel.moveToNextTurn() },
                    onNextWord = { viewModel.moveToNextTurn() },
                    onPause = { viewModel.pauseGame() }
                )
            }
        }
        
        ScreenState.SUMMARY -> {
            if (gameState != null) {
                SummaryScreen(
                    gameState = gameState!!,
                    onContinue = { viewModel.continueGame() },
                    onFinishGame = { viewModel.finishGame() }
                )
            }
        }
        
        ScreenState.FINAL -> {
            if (gameState != null) {
                FinalScreen(
                    gameState = gameState!!,
                    onPlayAgain = { viewModel.showSetupScreen() },
                    onExit = { viewModel.showHomeScreen() }
                )
            }
        }
    }
}
