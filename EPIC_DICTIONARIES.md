# EPIC_DICTIONARIES.md — Dictionary Import, Difficulty and Word Packs

## Цель

Добавить нормальную систему словарей для игры:

- хранение слов в БД;
- разделение по сложности;
- категории;
- импорт из JSON;
- возможность обновлять и добавлять словари позже.

## MVP

- встроенный русский словарь;
- формат JSON;
- импорт в локальную БД при первом запуске;
- выбор слов по сложности;
- выбор размера пачки;
- исключение слов, уже занятых активными сессиями.

## Out of Scope

- онлайн-синхронизация;
- сервер словарей;
- пользовательский редактор слов;
- автоматическая генерация сложности без ручной проверки.

## Suggested Format

```json
{
  "dictionaryId": "ru_default_v1",
  "language": "ru",
  "version": 1,
  "words": [
    {
      "id": "ru_easy_0001",
      "text": "самолёт",
      "difficulty": "easy",
      "category": "transport",
      "enabled": true
    }
  ]
}
````

## Acceptance Criteria

* приложение импортирует словарь из JSON;
* слова сохраняются в локальную БД;
* пользователь может выбрать сложность;
* bulk собирается только из доступных слов;
* активные сессии не получают пересекающиеся слова;
* при нехватке слов приложение показывает понятную ошибку.

```

Твой текущий `listOf(...)` нормален для smoke-test, но следующий шаг лучше такой: сделать `ru_default_words.json` на 200–300 слов вручную, поделить на `easy / medium / hard`, а уже потом подключать большие частотные списки.
::contentReference[oaicite:5]{index=5}
```

[1]: https://ruscorpora.ru/en/page/tool-freq/?utm_source=chatgpt.com "Frequency dictionaries"
[2]: https://www.artint.ru/projects/frqlist/frqlist-en.php?utm_source=chatgpt.com "The frequency dictionary for Russian"
[3]: https://github.com/hingston/russian?utm_source=chatgpt.com "hingston/russian: This repo contains a list of the 100000 ..."
[4]: https://github.com/Badestrand/russian-dictionary?utm_source=chatgpt.com "Badestrand/russian-dictionary: Dataset of nouns, verbs ..."
[5]: https://en.wiktionary.org/wiki/Wiktionary%3AFrequency_lists/Russian?utm_source=chatgpt.com "Wiktionary:Frequency lists/Russian"
