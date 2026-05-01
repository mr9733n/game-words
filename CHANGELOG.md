# Changelog

## [1.0.0] - 2026-05-01

### Added
- Core game data models (Word, Team, GameSettings, GameState)
- WordDictionary for managing word selection and exclusion logic
- GameManager for handling core game logic including state management and turn progression
- GamePersistence layer with DataStore and JSON serialization for saving/loading game state
- UI components:
  - HomeScreen for starting new or resuming games
  - SetupScreen for configuring game settings
  - GameScreen for main gameplay with timer and word display
  - SummaryScreen for round/game summaries
  - FinalScreen for displaying final results
- Main Activity and ViewModel architecture
- Build scripts and project configuration

### Features Implemented
- Word bulk selection with configurable sizes (40/60/80/100)
- Multiple teams support
- 4 distinct game rounds with different rules
- Timer-based turns with pause functionality
- Fair word cycling between teams
- Persistent game state across app restarts
- Prevention of word overlap between active game sessions
- Proper scoring system with team tracking
- Round completion detection and progression

### Technical Details
- Built with Kotlin and Jetpack Compose
- Uses DataStore for persistence
- Implements proper error handling throughout
- Follows MVVM architecture pattern
- Supports offline gameplay
