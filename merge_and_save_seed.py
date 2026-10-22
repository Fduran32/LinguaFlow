# -*- coding: utf-8 -*-
"""
Merge and validate the full seed_content.json dataset
"""
import json
import sys
from generator_english import get_english_b1_to_c2
from generator_italian import get_italian_a2_to_c2
from generator_chinese import get_chinese_a2_to_c2

SEED_PATH = r"app\src\main\assets\seed_content.json"

with open(SEED_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

for lang in data["languages"]:
    lang_id = lang["id"]
    if lang_id == "en":
        # Keep A1 and A2
        a1_a2 = [lvl for lvl in lang["levels"] if lvl["cefr_code"] in ["A1", "A2"]]
        b1_to_c2 = get_english_b1_to_c2()
        lang["levels"] = a1_a2 + b1_to_c2
    elif lang_id == "it":
        # Keep A1
        a1 = [lvl for lvl in lang["levels"] if lvl["cefr_code"] == "A1"]
        a2_to_c2 = get_italian_a2_to_c2()
        lang["levels"] = a1 + a2_to_c2
    elif lang_id == "zh":
        # Keep A1
        a1 = [lvl for lvl in lang["levels"] if lvl["cefr_code"] == "A1"]
        a2_to_c2 = get_chinese_a2_to_c2()
        lang["levels"] = a1 + a2_to_c2

# Validate
total_questions = 0
for lang in data["languages"]:
    levels = lang["levels"]
    print(f"Language: {lang['name']} ({lang['id']}) -> Total Levels: {len(levels)}")
    cefr_counts = {}
    for lvl in levels:
        c = lvl["cefr_code"]
        cefr_counts[c] = cefr_counts.get(c, 0) + 1
        for les in lvl["lessons"]:
            total_questions += len(les["questions"])
    print(f"  Distribution: {cefr_counts}")
    assert len(levels) == 66, f"Expected 66 levels for {lang['id']}, found {len(levels)}"
    for c in ["A1", "A2", "B1", "B2", "C1", "C2"]:
        assert cefr_counts[c] == 11, f"Expected 11 levels for {lang['id']} {c}, found {cefr_counts.get(c)}"

print(f"Total multimodal questions across all languages and CEFR levels: {total_questions}")

# Write to file
with open(SEED_PATH, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"Successfully saved updated {SEED_PATH}!")
