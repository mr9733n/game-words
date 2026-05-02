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
MAX_WORDS = 1200
RELATIVES = {
    "мать", "отец", "сын", "дочь", "брат", "сестра",
    "муж", "жена", "дед", "бабка", "бабушка", "дедушка",
    "дядя", "тетя", "тесть", "теща", "свекор", "свекровь",
    "зять", "невестка", "внук", "внучка", "родитель", "родственник",
    "мама",
}
MONTHS = {
    "январь", "февраль", "март", "апрель", "май", "июнь",
    "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь",
}
CONTINENTS = {
    "европа", "азия", "африка", "америка", "австралия", "антарктида",
    "евразия",
}
COUNTRIES_NATIONALITIES = {
    "россия", "украина", "беларусь", "германия", "франция", "италия",
    "испания", "англия", "китай", "япония", "индия", "израиль",
    "сша", "америка", "американец", "польша", "турция", "грузия", "армения",
    "русский", "украинец", "белорус", "немец", "француз", "итальянец",
    "испанец", "англичанин", "китаец", "японец", "индус", "еврей",
    "израильтянин", "поляк", "турок", "грузин", "армянин",
}
NAMES = {
    "иван", "петр", "павел", "сергей", "алексей", "андрей", "дмитрий",
    "николай", "александр", "михаил", "владимир", "юрий", "василий",
    "мария", "анна", "елена", "наталья", "ольга", "татьяна", "ирина",
}
IDEOLOGY_HISTORY = {
    "князь", "царь", "боярин", "колхоз", "комиссар", "коммунист",
    "партия", "совет", "пролетарий", "пионер", "комсомол",
}
INSULTS = {
    "дурак", "идиот", "болван", "глупец", "негодяй", "мерзавец",
}
AMBIGUOUS_OR_BAD_GAME_WORDS = {
    "курс",
    "раз",
    "тут",
    "стать",
}
DIRECTIONS_POSITIONS = {
    "верх", "низ", "лево", "право", "север", "юг", "запад", "восток",
    "северо-восток", "северо-запад", "юго-восток", "юго-запад",
}

TIME_UNITS = {
    "час", "минута", "секунда", "сутки", "неделя", "месяц", "год",
}
NUMBERS = {
    "ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь",
    "восемь", "девять", "десять", "одиннадцать", "двенадцать",
    "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать",
    "семнадцать", "восемнадцать", "девятнадцать", "двадцать",
    "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят",
    "восемьдесят", "девяносто", "сто", "двести", "триста",
    "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот",
    "девятьсот", "тысяча", "миллион", "миллиард",
}
MILITARY_RANKS = {
    "солдат", "ефрейтор", "сержант", "старшина", "прапорщик",
    "лейтенант", "капитан", "майор", "подполковник", "полковник",
    "генерал", "маршал", "адмирал", "рядовой",
}
RUSSIAN_CITIES = {
    "москва", "петербург", "ленинград", "новгород", "екатеринбург",
    "казань", "самара", "омск", "пермь", "уфа", "воронеж",
    "краснодар", "саратов", "тюмень", "иркутск", "ярославль",
    "владивосток", "сочи", "калининград",
}

ADMIN_WORDS = {
    "министерство", "ведомство", "управление", "департамент",
    "администрация", "комитет", "комиссия", "канцелярия",
    "устав", "постановление", "распоряжение", "закон", "кодекс",
    "документ", "справка", "заявление", "отчет", "протокол",
}
DUPLICATE_MEANING_WORDS = {
    "кусочек", "домик", "столик", "котик", "носик", "глазок",
}
BLACKLIST = {
    "стать", "тут", "раз", "ага", "баба", "бабка",
    "бог", "брат", "внук", "девица", "девочка",
    "девчонка", "действие", "дон", "дочка",
    "дядя", "дурак", "бой", "вид", "база",
    "бег", "взвод", "влияние", "бригадир",
    "господь", "губернатор", "даль",
    "девка", "депутат", "дура",
    "дума", "значение", "изменение",
    "имя", "мальчик", "милиционер",
    "милиция", "мол", "молодец",
    "молодежь", "молодость",

    *RELATIVES,
    *MONTHS,
    *CONTINENTS,
    *COUNTRIES_NATIONALITIES,
    *NAMES,
    *IDEOLOGY_HISTORY,
    *INSULTS,
    *AMBIGUOUS_OR_BAD_GAME_WORDS,
    *DIRECTIONS_POSITIONS,
    *TIME_UNITS,
    *NUMBERS,
    *MILITARY_RANKS,
    *RUSSIAN_CITIES,
    *ADMIN_WORDS,
    *DUPLICATE_MEANING_WORDS,
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


def looks_like_diminutive(word: str) -> bool:
    if len(word) < 6:
        return False
    return word.endswith(DIMINUTIVE_SUFFIXES)


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

    if word in BLACKLIST:
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


def next_dictionary_version(output_path: Path) -> int:
    if not output_path.exists():
        return 1

    try:
        old_data = json.loads(output_path.read_text(encoding="utf-8"))
        return int(old_data.get("version", 0)) + 1
    except Exception:
        return 1


def main():
    words = []
    seen = set()

    with INPUT.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f, delimiter="\t")

        print("COLUMNS:", reader.fieldnames)

        for row in reader:
            raw = row.get("bare", "")
            word = raw.strip().lower()#.replace("ё", "е")
            translation = row.get("translations_en", "").lower()

            if any(marker in translation for marker in BAD_TRANSLATION_MARKERS):
                continue

            if not is_clean_word(word):
                continue

            if word in seen:
                continue

            seen.add(word)

            words.append({
                "id": f"ru_noun_{word}",
                "text": word,
                "difficulty": difficulty_for(word),
                "category": "noun",
                "enabled": True
            })

            if len(words) >= MAX_WORDS:
                break

    version = next_dictionary_version(OUTPUT)

    data = {
        "dictionaryId": "ru_default",
        "language": "ru",
        "version": version,
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