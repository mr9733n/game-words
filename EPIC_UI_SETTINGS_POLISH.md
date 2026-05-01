# EPIC_UI_SETTINGS_POLISH.md — Settings, Controls, UX Polish and Records

## 1. Цель

Доработать приложение после рабочего MVP:

- добавить экран настроек;
- добавить сервисные кнопки управления игрой;
- улучшить визуальный UX;
- добавить звуки/сигналы;
- добавить имена и цвета команд;
- подготовить хранение результатов как доску рекордов.

---

## 2. Новые экраны

### 2.1 Settings Screen

Доступ с главного экрана.

Кнопки/настройки:

- сбросить сохранённую игру;
- сбросить занятые слова / active words;
- загрузить словарь;
- выбрать режим:
  - Test Mode;
  - Game Mode;
- выбрать тему:
  - System;
  - Light;
  - Dark.

---

## 3. Test Mode / Game Mode

### Test Mode

Для разработки и проверки логики.

Поведение:

- минимальный размер пачки;
- короткий таймер;
- меньше команд;
- можно отключить уникальность слов между сессиями.

Пример defaults:

```kotlin
bulkSize = 5
teamCount = 2
roundCount = 2
turnDurationSeconds = 15
````

### Game Mode

Нормальная игра.

Пример defaults:

```kotlin
bulkSize = 40
teamCount = 2
roundCount = 4
turnDurationSeconds = 60
```

---

## 4. Сервисные игровые кнопки

Добавить в Game/Summary/Settings:

### Pause

Уже правильно:

```kotlin
fun pauseGame() {
    stopTimer()
}
```

Пауза не должна переводить на HOME.

---

### Restart Round

Сбросить только текущий раунд:

* все слова → `AVAILABLE`;
* `round` не меняется;
* `teamIndex` выбирается по правилу старта раунда;
* `wordIndex = 0`;
* очки текущего раунда желательно откатить позже, но для MVP можно явно предупредить, что общий score не сбрасывается.

---

### Restart Game

Сбросить всю игру:

* `round = 1`;
* `score = 0`;
* `roundScore = 0`;
* все слова → `AVAILABLE`;
* первая команда → стартовая.

---

### Reset State

Сервисная кнопка в Settings:

* удалить сохранённый `GameState`;
* очистить active words;
* вернуться на HOME.

---

## 5. Исправление roundScore

Сейчас у тебя уже есть:

```kotlin
data class Team(
    val id: String,
    val score: Int = 0,
    val roundScore: Int = 0
)
```

Но при `markWordAsGuessed()` надо увеличивать оба счёта:

```kotlin
this[currentTeamIndex] = this[currentTeamIndex].copy(
    score = this[currentTeamIndex].score + 1,
    roundScore = this[currentTeamIndex].roundScore + 1
)
```

А при переходе в следующий раунд:

```kotlin
val minRoundScore = currentState.teams.minOf { it.roundScore }
val nextRoundStartingTeamIndex = currentState.teams.indexOfFirst {
    it.roundScore == minRoundScore
}
```

После выбора стартующей команды надо сбросить `roundScore`:

```kotlin
val resetTeams = currentState.teams.map {
    it.copy(roundScore = 0)
}
```

И в `newState` использовать:

```kotlin
teams = resetTeams
```

Иначе правило “следующий раунд начинает команда, набравшая меньше за прошлый раунд” будет работать неправильно.

---

## 6. Таймер

### Красный цвет за 10 секунд

В `GameScreen.kt`:

```kotlin
val timerColor = if (timeLeft <= 10) {
    MaterialTheme.colors.error
} else {
    MaterialTheme.colors.onBackground
}
```

И в `Text` таймера:

```kotlin
Text(
    text = formatTime(timeLeft),
    color = timerColor,
    style = MaterialTheme.typography.h4,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(vertical = 16.dp)
)
```

---

## 7. Звуки

### Конец таймера

Для MVP можно использовать стандартный звук уведомления:

```kotlin
val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
val ringtone = RingtoneManager.getRingtone(context, notification)
ringtone.play()
```

Лучше сделать отдельный helper:

```kotlin
class SoundPlayer(private val context: Context) {
    fun playNotification() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, uri)?.play()
    }
}
```

Вызывать:

* когда таймер дошёл до 0;
* при старте нового раунда;
* можно отдельно при старте хода.

---

## 8. Имена и цвета команд

### Модель Team

Расширить:

```kotlin
data class Team(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val score: Int = 0,
    val roundScore: Int = 0
)
```

Default:

```text
Team 1 — color 0
Team 2 — color 1
Team 3 — color 2
```

Позже можно добавить:

* иконку;
* аватар;
* эмодзи.

---

## 9. Доска рекордов

После завершения игры сохранять результат:

```kotlin
data class GameRecord(
    val gameId: String,
    val finishedAt: Long,
    val teams: List<TeamRecord>,
    val winnerTeamId: String?,
    val isTie: Boolean,
    val roundCount: Int,
    val bulkSize: Int
)
```

На HOME добавить:

* `Records`;
* список завершённых игр;
* победитель;
* счёт;
* дата.

---

## 10. Тёмная тема

Сейчас используется старый `MaterialTheme`.

Нужно сделать хотя бы системную тему:

```kotlin
val darkTheme = isSystemInDarkTheme()
```

И обернуть приложение в кастомный theme composable.

Для MVP:

* System theme only;
* manual Light/Dark позже через Settings.

---

## 11. Улучшение GameScreen

Проблема: нижние системные кнопки Android мешают игровым кнопкам.

Решение:

* поднять action-кнопки выше;
* увеличить высоту кнопок;
* сделать `Guessed` самой большой/заметной;
* использовать `navigationBarsPadding()`.

Пример:

```kotlin
modifier = Modifier
    .fillMaxWidth()
    .navigationBarsPadding()
    .padding(bottom = 24.dp)
```

Для кнопок:

```kotlin
Modifier
    .fillMaxWidth()
    .height(64.dp)
```

---

## 12. Современный UI

Минимальный polish:

* крупная карточка слова;
* цвет команды в header;
* более крупный timer;
* большие кнопки;
* Material 3 позже;
* нормальные отступы;
* отдельный экран “Prepare Turn” перед стартом;
* `Start Turn` не должен быть маленькой кнопкой в середине экрана.

---

## 13. Acceptance Criteria

* с HOME можно открыть Settings;
* можно сбросить сохранённый state;
* можно очистить active words;
* есть Test Mode и Game Mode;
* Test Mode позволяет быстро проверить игру на маленькой пачке;
* `roundScore` считается отдельно от общего score;
* следующий раунд начинает команда с меньшим `roundScore`;
* последние 10 секунд таймер красный;
* по окончании таймера играет стандартный notification sound;
* команды имеют имена и цвета;
* финальные результаты сохраняются в records;
* тёмная системная тема применяется;
* игровые кнопки не перекрываются системной навигацией.

````

Самый важный **первый фикс сейчас** — `roundScore`. У тебя поле уже добавлено, но в текущем `markWordAsGuessed()` увеличивается только `score`. Поэтому правило следующего раунда надо довести так:

```kotlin
this[currentTeamIndex] = this[currentTeamIndex].copy(
    score = this[currentTeamIndex].score + 1,
    roundScore = this[currentTeamIndex].roundScore + 1
)
````

А в `continueGame()` выбирать стартующую команду по `roundScore`, не по `score`, и потом сбрасывать `roundScore` у всех команд.
