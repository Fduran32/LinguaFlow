# -*- coding: utf-8 -*-
"""
Script to generate full seed_content.json containing:
- 3 Languages: English (en), Italian (it), Mandarin Chinese (zh)
- 6 CEFR Levels per language: A1, A2, B1, B2, C1, C2
- 10 Sub-levels per CEFR level (each with 4 multi-modal exercises: TRANSLATE_BUILD, LISTENING, SPEAKING, MULTIPLE_CHOICE)
- 1 Graduation Exam per CEFR level (orderIndex = 11, with 10 multi-modal questions)
Total per language: 11 nodes * 6 CEFR = 66 nodes!
"""

import json
import os

# We will read existing A1 and A2 for English, A1 for Italian, A1 for Chinese,
# and programmatically expand all missing sub-levels and graduation exams with rich, authentic curriculum.

SEED_PATH = r"C:\Users\felip\Desktop\Proyecto_App_Idiomas\LinguaFlow\app\src\main\assets\seed_content.json"

print(f"Reading current seed from: {SEED_PATH}")
with open(SEED_PATH, "r", encoding="utf-8") as f:
    current_data = json.load(f)

print(f"Loaded {len(current_data['languages'])} languages.")
