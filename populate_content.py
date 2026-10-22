# -*- coding: utf-8 -*-
"""
Full Content Generator for LinguaFlow
Generates 10 sublevels + 1 exam for:
- English B1, B2, C1, C2
- Italian A2, B1, B2, C1, C2
- Chinese A2, B1, B2, C1, C2
"""
import json
import os
from seed_helpers import make_sublevel, make_exam

SEED_PATH = r"C:\Users\felip\Desktop\Proyecto_App_Idiomas\LinguaFlow\app\src\main\assets\seed_content.json"

with open(SEED_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

# English B1, B2, C1, C2 data
en_b1_sublevels = [
    ("Viajes y Vacaciones", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'While I was traveling, I ___ many historic places.'", "audio": "While I was traveling, I visited many historic places.", "options": ["visited", "visiting", "visit", "was visit"], "correct": "visited", "explanation": "Past simple para una acción puntual mientras ocurría otra en past continuous."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena las fichas: 'Perdí mi vuelo ayer por la tarde'", "audio": "I missed my flight yesterday afternoon", "options": ["I", "missed", "my", "flight", "yesterday", "afternoon", "lost", "plane"], "correct": "I missed my flight yesterday afternoon", "explanation": "Se usa 'miss a flight' para perder un vuelo."},
        {"type": "SPEAKING", "prompt": "Toca el micrófono y pronuncia en inglés:", "audio": "Where is the baggage claim area?", "options": [], "correct": "Where is the baggage claim area?", "explanation": "Pronunciación: /wɛər ɪz ðə ˈbæɡɪdʒ kleɪm ˈɛəriə/."},
        {"type": "LISTENING", "prompt": "Escucha y selecciona la opción correcta:", "audio": "Could you tell me the platform number?", "options": ["Could you tell me the platform number?", "Could you tell me the flight number?", "Can you buy the platform ticket?", "Where is the train conductor?"], "correct": "Could you tell me the platform number?", "explanation": "El audio dice: 'Could you tell me the platform number?'."}
    ]),
    ("Trabajo y Profesiones", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'She has worked in this company ___ 2018.'", "audio": "She has worked in this company since 2018.", "options": ["since", "for", "during", "from"], "correct": "since", "explanation": "'Since' se usa con puntos temporales específicos."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Tengo una entrevista de trabajo mañana'", "audio": "I have a job interview tomorrow", "options": ["I", "have", "a", "job", "interview", "tomorrow", "work", "meeting"], "correct": "I have a job interview tomorrow", "explanation": "'Job interview' es entrevista de trabajo."},
        {"type": "SPEAKING", "prompt": "Pronuncia tu respuesta en inglés:", "audio": "I am sending my updated resume", "options": [], "correct": "I am sending my updated resume", "explanation": "Resume: currículum vítae."},
        {"type": "LISTENING", "prompt": "Escucha atentamente:", "audio": "The manager postponed the team meeting.", "options": ["The manager postponed the team meeting.", "The manager canceled the team meeting.", "The manager opened the team meeting.", "The manager started the meeting."], "correct": "The manager postponed the team meeting.", "explanation": "'Postponed' significa pospuso."}
    ]),
    ("Salud y Bienestar", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'You look exhausted; you ___ get some rest.'", "audio": "You look exhausted; you should get some rest.", "options": ["should", "must have", "can to", "ought"], "correct": "should", "explanation": "'Should' se usa para dar recomendaciones y consejos."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Tengo dolor de cabeza y fiebre'", "audio": "I have a headache and a fever", "options": ["I", "have", "a", "headache", "and", "a", "fever", "pain", "ill"], "correct": "I have a headache and a fever", "explanation": "Expresiones de síntomas médicos en inglés."},
        {"type": "SPEAKING", "prompt": "Pronuncia la recomendación médica:", "audio": "You must take this medicine twice a day", "options": [], "correct": "You must take this medicine twice a day", "explanation": "Must expresa prescripción médica obligatoria."},
        {"type": "LISTENING", "prompt": "Escucha y responde:", "audio": "Drink plenty of water and stay in bed.", "options": ["Drink plenty of water and stay in bed.", "Drink plenty of tea and go outside.", "Eat less sugar and stay in bed.", "Drink warm milk and go to sleep."], "correct": "Drink plenty of water and stay in bed.", "explanation": "'Drink plenty of water and stay in bed.'"}
    ]),
    ("Medio Ambiente", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Primer condicional: 'If we recycle more, we ___ pollution.'", "audio": "If we recycle more, we will reduce pollution.", "options": ["will reduce", "would reduce", "reduced", "reduce"], "correct": "will reduce", "explanation": "First conditional: If + present simple, will + verb."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Debemos proteger la vida silvestre'", "audio": "We must protect endangered wildlife", "options": ["We", "must", "protect", "endangered", "wildlife", "animals", "save"], "correct": "We must protect endangered wildlife", "explanation": "'Endangered wildlife' significa vida silvestre amenazada."},
        {"type": "SPEAKING", "prompt": "Pronuncia la frase ecológica:", "audio": "Solar energy is completely renewable", "options": [], "correct": "Solar energy is completely renewable", "explanation": "Renewable energy."},
        {"type": "LISTENING", "prompt": "Escucha la advertencia climática:", "audio": "Global temperatures are rising steadily.", "options": ["Global temperatures are rising steadily.", "Global temperatures are dropping slowly.", "Local temperatures are changing daily.", "Ocean levels are staying normal."], "correct": "Global temperatures are rising steadily.", "explanation": "Temperaturas globales en aumento continuo."}
    ]),
    ("Tecnología y Redes", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Voz pasiva: 'Millions of smartphones ___ every year.'", "audio": "Millions of smartphones are sold every year.", "options": ["are sold", "were sell", "is sold", "sell"], "correct": "are sold", "explanation": "Passive voice: are + past participle (sold)."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Descargué una nueva aplicación ayer'", "audio": "I downloaded a new application yesterday", "options": ["I", "downloaded", "a", "new", "application", "yesterday", "app", "installed"], "correct": "I downloaded a new application yesterday", "explanation": "Verbo download en pasado regular: downloaded."},
        {"type": "SPEAKING", "prompt": "Pronuncia la frase técnica:", "audio": "Please enter your password to connect", "options": [], "correct": "Please enter your password to connect", "explanation": "Pronunciación estándar."},
        {"type": "LISTENING", "prompt": "Escucha atentamente el audio:", "audio": "The server connection was lost suddenly.", "options": ["The server connection was lost suddenly.", "The internet speed increased quickly.", "The computer screen turned off suddenly.", "The database was deleted completely."], "correct": "The server connection was lost suddenly.", "explanation": "'The server connection was lost suddenly.'"}
    ]),
    ("Hábitos Pasados", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'I used to ___ tennis when I was young.'", "audio": "I used to play tennis when I was young.", "options": ["play", "playing", "played", "plays"], "correct": "play", "explanation": "'Used to' va seguido de infinitivo sin 'to'."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Solíamos vivir cerca de la playa'", "audio": "We used to live near the beach", "options": ["We", "used", "to", "live", "near", "the", "beach", "house", "at"], "correct": "We used to live near the beach", "explanation": "Used to live: solíamos vivir."},
        {"type": "SPEAKING", "prompt": "Pronuncia tu recuerdo:", "audio": "Did you use to walk to school?", "options": [], "correct": "Did you use to walk to school?", "explanation": "Pregunta con did you use to."},
        {"type": "LISTENING", "prompt": "Escucha y responde:", "audio": "He would always read before going to sleep.", "options": ["He would always read before going to sleep.", "He would always write before dinner.", "He used to sleep during reading.", "He never read at night."], "correct": "He would always read before going to sleep.", "explanation": "'Would' para hábitos repetidos en el pasado."}
    ]),
    ("Comida y Restaurantes", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'There isn't ___ sugar left in the jar.'", "audio": "There isn't much sugar left in the jar.", "options": ["much", "many", "a few", "several"], "correct": "much", "explanation": "Sugar es incontable, en negativas se usa 'much'."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '¿Puedo ver el menú por favor?'", "audio": "May I see the menu please?", "options": ["May", "I", "see", "the", "menu", "please", "can", "look"], "correct": "May I see the menu please?", "explanation": "Petición formal con May I."},
        {"type": "SPEAKING", "prompt": "Pronuncia la orden:", "audio": "Could we have the bill please?", "options": [], "correct": "Could we have the bill please?", "explanation": "Bill: la cuenta del restaurante."},
        {"type": "LISTENING", "prompt": "Escucha la recomendación del chef:", "audio": "Today's special dish is grilled salmon.", "options": ["Today's special dish is grilled salmon.", "The soup of the day is fresh chicken.", "We recommend the fried fish with fries.", "Our chef prepared baked vegetables."], "correct": "Today's special dish is grilled salmon.", "explanation": "Salmón a la plancha."}
    ]),
    ("Vivienda y Ciudad", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Cláusula relativa: 'This is the city ___ I was born.'", "audio": "This is the city where I was born.", "options": ["where", "which", "who", "whose"], "correct": "where", "explanation": "Para lugares se usa el pronombre relativo 'where'."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'El departamento tiene dos dormitorios luminosos'", "audio": "The apartment has two bright bedrooms", "options": ["The", "apartment", "has", "two", "bright", "bedrooms", "flat", "rooms"], "correct": "The apartment has two bright bedrooms", "explanation": "Adjetivo 'bright' antes del sustantivo."},
        {"type": "SPEAKING", "prompt": "Pronuncia la descripción:", "audio": "The neighborhood is quiet and very safe", "options": [], "correct": "The neighborhood is quiet and very safe", "explanation": "Neighborhood: barrio / vecindario."},
        {"type": "LISTENING", "prompt": "Escucha las características:", "audio": "The balcony overlooks a beautiful park.", "options": ["The balcony overlooks a beautiful park.", "The kitchen has new modern appliances.", "The bedroom windows face the busy street.", "The garden has many colorful flowers."], "correct": "The balcony overlooks a beautiful park.", "explanation": "Overlooks: da hacia / tiene vista a."}
    ]),
    ("Dinero y Compras", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Segundo condicional: 'If I had enough money, I ___ that car.'", "audio": "If I had enough money, I would buy that car.", "options": ["would buy", "will buy", "bought", "buy"], "correct": "would buy", "explanation": "Second conditional: If + past simple, would + infinitive."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: '¿Aceptan tarjeta de crédito o efectivo?'", "audio": "Do you accept credit cards or cash?", "options": ["Do", "you", "accept", "credit", "cards", "or", "cash", "money"], "correct": "Do you accept credit cards or cash?", "explanation": "Pregunta de método de pago estándar."},
        {"type": "SPEAKING", "prompt": "Pronuncia la consulta de precio:", "audio": "Is there any discount on this item?", "options": [], "correct": "Is there any discount on this item?", "explanation": "Discount on this item."},
        {"type": "LISTENING", "prompt": "Escucha la confirmación:", "audio": "You can keep the receipt for warranty.", "options": ["You can keep the receipt for warranty.", "Please sign the bill at the bottom.", "The return policy is thirty days.", "We do not offer cash refunds."], "correct": "You can keep the receipt for warranty.", "explanation": "Receipt: recibo de compra."}
    ]),
    ("Planes Futuros", [
        {"type": "MULTIPLE_CHOICE", "prompt": "Plan fijado: 'We ___ to Rome next Friday.'", "audio": "We are flying to Rome next Friday.", "options": ["are flying", "will fly", "fly", "flown"], "correct": "are flying", "explanation": "Present continuous para planes cerrados y agendados con fecha."},
        {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Voy a empezar un nuevo curso'", "audio": "I am going to start a new course", "options": ["I", "am", "going", "to", "start", "a", "new", "course", "begin"], "correct": "I am going to start a new course", "explanation": "Be going to para intenciones planificadas."},
        {"type": "SPEAKING", "prompt": "Pronuncia tu meta:", "audio": "I hope to speak English fluently soon", "options": [], "correct": "I hope to speak English fluently soon", "explanation": "Fluently: con fluidez."},
        {"type": "LISTENING", "prompt": "Escucha la predicción:", "audio": "I think artificial intelligence will change everything.", "options": ["I think artificial intelligence will change everything.", "I believe machine learning is becoming popular.", "Scientists say computers will replace phones.", "Technology is developing at rapid speeds."], "correct": "I think artificial intelligence will change everything.", "explanation": "Predicción con will."}
    ])
]

en_b1_exam_questions = [
    {"type": "MULTIPLE_CHOICE", "prompt": "Completa: 'She has been working here ___ six months.'", "audio": "She has been working here for six months.", "options": ["for", "since", "during", "ago"], "correct": "for", "explanation": "'For' se usa para duraciones de tiempo."},
    {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Si llueve mañana, nos quedaremos en casa'", "audio": "If it rains tomorrow we will stay home", "options": ["If", "it", "rains", "tomorrow", "we", "will", "stay", "home", "would"], "correct": "If it rains tomorrow we will stay home", "explanation": "First conditional."},
    {"type": "SPEAKING", "prompt": "Pronuncia la frase:", "audio": "Could you please show me your passport?", "options": [], "correct": "Could you please show me your passport?", "explanation": "Petición formal."},
    {"type": "LISTENING", "prompt": "Escucha y responde:", "audio": "The train departs from platform three at noon.", "options": ["The train departs from platform three at noon.", "The bus arrives at gate three at midnight.", "The flight leaves at three in the afternoon.", "The boat sails at two o'clock."], "correct": "The train departs from platform three at noon.", "explanation": "Platform three at noon."},
    {"type": "MULTIPLE_CHOICE", "prompt": "Pasado pasivo: 'The famous painting ___ by Picasso in 1937.'", "audio": "The famous painting was created by Picasso in 1937.", "options": ["was created", "is created", "created", "were created"], "correct": "was created", "explanation": "Was + past participle."},
    {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Solía montar en bicicleta todos los días'", "audio": "I used to ride my bike every day", "options": ["I", "used", "to", "ride", "my", "bike", "every", "day", "cycle"], "correct": "I used to ride my bike every day", "explanation": "Used to para hábitos del pasado."},
    {"type": "SPEAKING", "prompt": "Pronuncia:", "audio": "If I had more time I would travel", "options": [], "correct": "If I had more time I would travel", "explanation": "Second conditional."},
    {"type": "LISTENING", "prompt": "Escucha atentamente:", "audio": "You must take two tablets every eight hours.", "options": ["You must take two tablets every eight hours.", "You should drink water every six hours.", "Take one pill before going to bed.", "Drink this medicine twice a week."], "correct": "You must take two tablets every eight hours.", "explanation": "Dosis médica."},
    {"type": "MULTIPLE_CHOICE", "prompt": "Cláusula relativa: 'The doctor ___ treated me was very kind.'", "audio": "The doctor who treated me was very kind.", "options": ["who", "which", "where", "whose"], "correct": "who", "explanation": "'Who' se usa para personas."},
    {"type": "TRANSLATE_BUILD", "prompt": "Ordena: 'Vamos a celebrar una fiesta el sábado'", "audio": "We are going to have a party on Saturday", "options": ["We", "are", "going", "to", "have", "a", "party", "on", "Saturday"], "correct": "We are going to have a party on Saturday", "explanation": "Going to have a party."}
]

print("Script template ready.")
