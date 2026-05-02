# F:\9834758345hf7A\game-words\app\src\main\assets\dictionaries\ru_default_words.json
#
import csv
import json
import re
from pathlib import Path

ROOT = Path.cwd()

INPUT = ROOT / "input" / "nouns.csv"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "dictionaries" / "ru_default_words.json"
print("ROOT:", ROOT)
print("INPUT:", INPUT)
print("OUTPUT:", OUTPUT)
CYRILLIC_RE = re.compile(r"^[а-яёА-ЯЁ-]+$")

BAD_PARTS = [
    "*", "/", "\\", "(", ")", "[", "]", "{", "}",
    ".", ",", ";", ":", "!", "?", "\"", "'",
    "́"
]

STOP_WORDS = {
    "аббревиатура",
    "буква",
}

def is_clean_word(word: str) -> bool:
    word = word.strip().lower()

    if not word:
        return False

    if len(word) < 3:
        return False

    if len(word) > 16:
        return False

    if any(part in word for part in BAD_PARTS):
        return False

    if not CYRILLIC_RE.match(word):
        return False

    if word in STOP_WORDS:
        return False

    if "-" in word:
        return False

    return True


def difficulty_for(word: str) -> str:
    length = len(word)

    if length <= 7:
        return "easy"

    if length <= 11:
        return "medium"

    return "hard"


def main():
    words = []
    seen = set()

    with INPUT.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f, delimiter="\t")

        print("COLUMNS:", reader.fieldnames)

        for row in reader:
            raw = row.get("bare", "")
            word = raw.strip().lower().replace("ё", "е")

            if not is_clean_word(word):
                continue

            if word in seen:
                continue

            seen.add(word)

            words.append({
                "id": f"ru_noun_{len(words) + 1:06d}",
                "text": word,
                "difficulty": difficulty_for(word),
                "category": "noun",
                "enabled": True
            })

    data = {
        "dictionaryId": "ru_default_v1",
        "language": "ru",
        "version": 1,
        "source": "OpenRussian / Badestrand russian-dictionary",
        "license": "CC-BY-SA-4.0",
        "words": words
    }

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        json.dumps(data, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )

    print(f"Saved {len(words)} words to {OUTPUT}")


if __name__ == "__main__":
    main()