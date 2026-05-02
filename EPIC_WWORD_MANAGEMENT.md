# EPIC_WORD_MANAGEMENT.md

## Цель

Дать пользователю возможность выключать отдельные слова из игрового словаря.

## Scope

- Экран Word Management
- Список слов из локальной БД
- Поиск по тексту
- Фильтр по сложности
- Переключатель enabled/disabled
- Bulk actions:
  - Enable all
  - Disable all filtered
  - Reset dictionary

## Data Model

words:
- id
- text
- difficulty
- category
- enabled
- source

## Flow

1. При первом запуске или по кнопке Import Dictionary JSON импортируется в DB.
2. SetupScreen выбирает bulk только из enabled words.
3. Word Management меняет поле enabled.
4. Disabled words не попадают в игру.

## Acceptance Criteria

- выключенное слово не появляется в новой игре;
- состояние enabled сохраняется после перезапуска;
- можно вернуть все слова обратно;
- test mode не зависит от DB.