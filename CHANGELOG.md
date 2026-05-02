# Changelog

## [1.0.3] - 2026-05-02

### Added

- JSON dictionary → import into Room database
- Word Management screen:
  - search words
  - difficulty filter (easy / medium / hard)
  - enable / disable words
  - enable filtered / disable filtered
  - show/hide disabled words
- Active words tracking to prevent reuse
- Error handling for insufficient words
- Records screen improvements:
  - scrollable list
  - expandable record cards
  - display of used words per game
- Timer notification sound
- Basic adaptive UI scaling

### Fixed

- Timer pause now preserves remaining time
- Word locking issues on Android 12
- Layout scaling issues on small screens
- LazyColumn usage fixes (scroll issues)

### Changed

- Word IDs switched to stable format (`ru_noun:{word}`)
- Dictionary import no longer duplicates words
- Import logic updated to preserve user `enabled` state

---

### Planned

- Proper Room migrations (remove destructive migration)
- Dictionary metadata persistence (source / license)
- Import custom dictionary from device
- Confirmation dialogs for destructive actions
- Full localization via `strings.xml`
- UI polish (Material 3, icons, theme)
- Improved dictionary filtering (blacklist/whitelist pipeline)
- Auto dictionary update by version

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
