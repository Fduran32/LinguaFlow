# -*- coding: utf-8 -*-
import json
import sys

def make_sublevel(lvl_id, order_index, title, cefr_code, prev_id, questions_data):
    # questions_data is list of 4 dicts: mc, tb, spk, lis
    questions = []
    for i, q in enumerate(questions_data, 1):
        questions.append({
            "id": f"{lvl_id}_q{i}",
            "type": q["type"],
            "prompt": q["prompt"],
            "prompt_phonetic": q.get("phonetic"),
            "prompt_audio_text": q.get("audio"),
            "options": q.get("options", []),
            "correct_answer": q["correct"],
            "explanation": q.get("explanation", "")
        })
    return {
        "id": lvl_id,
        "order_index": order_index,
        "title": f"Subnivel {order_index}: {title}",
        "cefr_code": cefr_code,
        "required_previous_level_id": prev_id,
        "lessons": [
            {
                "id": f"les_{lvl_id}",
                "order_index": 1,
                "title": f"Subnivel {order_index}: {title}",
                "xp_reward": 15,
                "questions": questions
            }
        ]
    }

def make_exam(lvl_id, order_index, title, cefr_code, prev_id, questions_data):
    questions = []
    for i, q in enumerate(questions_data, 1):
        questions.append({
            "id": f"{lvl_id}_ex{i}",
            "type": q["type"],
            "prompt": q["prompt"],
            "prompt_phonetic": q.get("phonetic"),
            "prompt_audio_text": q.get("audio"),
            "options": q.get("options", []),
            "correct_answer": q["correct"],
            "explanation": q.get("explanation", "")
        })
    return {
        "id": lvl_id,
        "order_index": order_index,
        "title": f"🎓 Examen Final de Certificación {cefr_code}",
        "cefr_code": cefr_code,
        "required_previous_level_id": prev_id,
        "lessons": [
            {
                "id": f"les_{lvl_id}",
                "order_index": 1,
                "title": f"🎓 Examen Final de Certificación {cefr_code}",
                "xp_reward": 30,
                "questions": questions
            }
        ]
    }

print("Helper definitions loaded successfully.")
