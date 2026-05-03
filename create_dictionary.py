import csv
import json
import re
import sys
from pathlib import Path

ROOT = Path.cwd()
INPUT = ROOT / "tools" / "input" / "nouns.csv"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "dictionaries" / "ru_default_words.json"

DICTIONARY_TOOLS = ROOT / "tools" / "dictionary"
BLACKLIST_DIR = DICTIONARY_TOOLS / "blacklist"
WHITELIST_DIR = DICTIONARY_TOOLS / "whitelist"

CURATION_DIR = DICTIONARY_TOOLS / "curation"
APPROVED_FILE = CURATION_DIR / "approved.txt"
DISABLED_FILE = CURATION_DIR / "disabled.txt"
REJECTED_FILE = CURATION_DIR / "rejected.txt"
CURRENT_BATCH = CURATION_DIR / "current_batch.csv"

BATCH_SIZE = 100
MAX_WORDS = 1200

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

DIMINUTIVE_SUFFIXES = (
    "очек", "ечек", "чик", "щик", "ик", "ек", "ок", "ушка", "юшка",
    "онька", "енька"
)

ABSTRACT_WORDS = {
    "случай",
    "смысл",
    "отношение",
    "состояние",
    "качество",
    "возможность",
    "часть",
    "сторона",
    "место",
}

BAD_TRANSLATION_MARKERS = [
    "archaic",
    "obsolete",
    "rare",
    "dialect",
    "botany",
    "zoology",
]


def load_word_set(path: Path) -> set[str]:
    if not path.exists():
        return set()

    text = path.read_text(encoding="utf-8")
    raw_items = re.split(r"[\n,]+", text)

    return {
        item.strip().lower().strip('"').strip("'")
        for item in raw_items
        if item.strip() and not item.strip().startswith("#")
    }


def save_word_set(path: Path, words: set[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "\n".join(sorted(words)) + ("\n" if words else ""),
        encoding="utf-8"
    )


def load_word_sets(directory: Path) -> set[str]:
    words = set()
    if not directory.exists():
        return words

    for path in directory.glob("*.txt"):
        words |= load_word_set(path)

    return words


def manual_blacklist() -> set[str]:
    return load_word_sets(BLACKLIST_DIR)


def manual_whitelist() -> set[str]:
    return load_word_sets(WHITELIST_DIR)


def approved_words() -> set[str]:
    return load_word_set(APPROVED_FILE)


def disabled_words() -> set[str]:
    return load_word_set(DISABLED_FILE)


def rejected_words() -> set[str]:
    return load_word_set(REJECTED_FILE)


def looks_like_diminutive(word: str) -> bool:
    if len(word) < 6:
        return False
    return word.endswith(DIMINUTIVE_SUFFIXES)


def is_clean_word(word: str, whitelist: set[str], blacklist: set[str]) -> bool:
    word = word.strip().lower()

    if word in whitelist:
        return True

    if not word:
        return False

    if len(word) < 3 or len(word) > 16:
        return False

    if any(part in word for part in BAD_PARTS):
        return False

    if not CYRILLIC_RE.match(word):
        return False

    if word in STOP_WORDS:
        return False

    if "-" in word:
        return False

    if word in blacklist:
        return False

    if looks_like_diminutive(word):
        return False

    return True


def difficulty_for(word: str) -> str:
    if word in ABSTRACT_WORDS:
        return "hard"

    length = len(word)

    if length <= 7:
        return "easy"

    if length <= 11:
        return "medium"

    return "hard"


def review_reasons(word: str, translation: str) -> list[str]:
    reasons = []

    if len(word) > 11:
        reasons.append("long")

    if word in ABSTRACT_WORDS:
        reasons.append("abstract")

    if looks_like_diminutive(word):
        reasons.append("diminutive")

    if "," in translation or ";" in translation:
        reasons.append("many_meanings")

    return reasons


def iter_candidates():
    whitelist = manual_whitelist()
    blacklist = manual_blacklist()

    with INPUT.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f, delimiter="\t")

        for row in reader:
            raw = row.get("bare", "")
            word = raw.strip().lower()
            translation = row.get("translations_en", "").lower()

            if any(marker in translation for marker in BAD_TRANSLATION_MARKERS):
                continue

            if not is_clean_word(word, whitelist, blacklist):
                continue

            yield {
                "word": word,
                "difficulty": difficulty_for(word),
                "translation": translation,
                "reasons": "|".join(review_reasons(word, translation)),
            }


def make_batch() -> None:
    CURATION_DIR.mkdir(parents=True, exist_ok=True)

    already_seen = (
        approved_words()
        | disabled_words()
        | rejected_words()
        | manual_blacklist()
    )

    batch = []
    used = set()

    for item in iter_candidates():
        word = item["word"]

        if word in already_seen:
            continue

        if word in used:
            continue

        used.add(word)
        batch.append({
            "word": word,
            "difficulty": item["difficulty"],
            "translation": item["translation"],
            "reasons": item["reasons"],
            "decision": "",
        })

        if len(batch) >= BATCH_SIZE:
            break

    with CURRENT_BATCH.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=["word", "difficulty", "translation", "reasons", "decision"]
        )
        writer.writeheader()
        writer.writerows(batch)

    print(f"Saved batch: {len(batch)} words -> {CURRENT_BATCH}")
    print("Fill decision column: approve / disable / reject")


def apply_batch() -> None:
    if not CURRENT_BATCH.exists():
        raise FileNotFoundError(f"Batch file not found: {CURRENT_BATCH}")

    approved = approved_words()
    disabled = disabled_words()
    rejected = rejected_words()

    with CURRENT_BATCH.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)

        for row in reader:
            word = row.get("word", "").strip().lower()
            decision = row.get("decision", "").strip().lower()

            if not word:
                continue

            if decision in {"approve", "approved", "a", "yes", "y"}:
                approved.add(word)
                disabled.discard(word)
                rejected.discard(word)

            elif decision in {"disable", "disabled", "d"}:
                disabled.add(word)
                approved.discard(word)
                rejected.discard(word)

            elif decision in {"reject", "rejected", "r", "no", "n"}:
                rejected.add(word)
                approved.discard(word)
                disabled.discard(word)

    save_word_set(APPROVED_FILE, approved)
    save_word_set(DISABLED_FILE, disabled)
    save_word_set(REJECTED_FILE, rejected)

    print(f"Approved: {len(approved)}")
    print(f"Disabled: {len(disabled)}")
    print(f"Rejected: {len(rejected)}")


def next_dictionary_version(output_path: Path) -> int:
    if not output_path.exists():
        return 1

    try:
        old_data = json.loads(output_path.read_text(encoding="utf-8"))
        return int(old_data.get("version", 0)) + 1
    except Exception:
        return 1


def build_json() -> None:
    approved = approved_words()
    disabled = disabled_words()

    selected = {}
    for item in iter_candidates():
        word = item["word"]

        if word in approved or word in disabled:
            selected[word] = item

        if len(selected) >= MAX_WORDS:
            break

    words = []

    for word in sorted(selected.keys()):
        item = selected[word]

        words.append({
            "id": f"ru_noun:{word}",
            "text": word,
            "difficulty": item["difficulty"],
            "category": "noun",
            "enabled": word in approved,
        })

    version = next_dictionary_version(OUTPUT)

    data = {
        "dictionaryId": "ru_default",
        "language": "ru",
        "version": version,
        "source": "OpenRussian / Badestrand russian-dictionary",
        "license": "CC-BY-SA-4.0",
        "words": words,
    }

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        json.dumps(data, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )

    print(f"Saved {len(words)} curated words to {OUTPUT}")
    print(f"Dictionary version: {version}")


def main() -> None:
    command = sys.argv[1] if len(sys.argv) > 1 else "help"

    print("ROOT:", ROOT)
    print("INPUT:", INPUT)
    print("OUTPUT:", OUTPUT)

    if command == "make-batch":
        make_batch()
    elif command == "apply-batch":
        apply_batch()
    elif command == "build-json":
        build_json()
    else:
        print("Usage:")
        print("  python create_dictionary.py make-batch")
        print("  python create_dictionary.py apply-batch")
        print("  python create_dictionary.py build-json")


if __name__ == "__main__":
    main()