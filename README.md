# Party Word Game

A party game similar to Alias where teams take turns explaining, acting out, or drawing words.

## Features

- Play with multiple teams
- Four distinct game rounds with different rules:
  1. Explanation Round - Describe the word without using related terms
  2. Charades Round - Act out the word with gestures only
  3. One Word Round - Give only a single clue word
  4. Drawing Round - Draw the word (offline)
- Configurable game settings (word bulk size, team count, round count, turn duration)
- Offline gameplay with persistent state
- Fair word distribution between teams
- Prevents word overlap between concurrent game sessions

## Getting Started

### Prerequisites

- Android Studio Flamingo or later
- Android SDK API level 34
- Kotlin 1.8+

### Building

1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Build and run the app

### Deployment

To generate a signed APK for release:

1. Go to Build > Generate Signed Bundle / APK
2. Select APK and follow the wizard
3. Use your keystore credentials
4. Select release build type

## Architecture

The app follows MVVM architecture pattern:

- **Models**: Game data structures (Word, Team, GameState, etc.)
- **View**: Compose UI components (HomeScreen, GameScreen, etc.)
- **ViewModel**: GameViewModel handling business logic and state management
- **Core**: GameManager with game logic implementation
- **Persistence**: DataStore-based persistence layer

## Tech Stack

- Kotlin
- Jetpack Compose
- DataStore Preferences
- Kotlin Serialization
- ViewModel + StateFlow

## License

This project is licensed under the MIT License - see the LICENSE file for details.
