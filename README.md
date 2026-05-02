# Party Word Game

A party game similar to Alias where teams take turns explaining, acting out, or drawing words.

## Features

## Features

- Play with multiple teams
- Four distinct game rounds with different rules:
  1. Explanation Round
  2. Charades Round
  3. One Word Round
  4. Drawing Round

- Configurable game settings:
  - word bulk size
  - team count
  - round count
  - turn duration
  - word difficulty

- Word dictionary system:
  - JSON dictionary import
  - Local Room database storage
  - Filtering by difficulty
  - Enable / disable words

- Word Management screen:
  - search words
  - filter by difficulty
  - toggle enabled/disabled
  - bulk enable/disable

- Records system:
  - game history
  - expandable records
  - list of used words per game

- Offline gameplay with persistent state
- Fair word distribution between teams
- Prevents word reuse between sessions


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

## Dictionary License

- OpenRussian / Badestrand russian-dictionary
- License: CC-BY-SA-4.0 
- See the THIRD_PARTY.md file for details.
- See https://github.com/Badestrand/russian-dictionary for details.