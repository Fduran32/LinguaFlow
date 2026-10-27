# AGENTS.md — Guía de Arquitectura, Pedagogía y Reglas para Agentes de IA

Este documento define las directrices técnicas, arquitectónicas y pedagógicas que cualquier Agente de IA (Google Antigravity, GitHub Copilot, Cursor, Claude Code, etc.) y desarrollador debe cumplir al trabajar en el repositorio **LinguaFlow**.

---

## 1. Descripción General del Proyecto

**LinguaFlow** es una aplicación móvil nativa para **Android** desarrollada en **Kotlin** con **Jetpack Compose**, diseñada para el aprendizaje estructurado y progresivo de **4 idiomas** (**Inglés 🇺🇸**, **Italiano 🇮🇹**, **Chino Mandarín 🇨🇳** y **Portugués 🇧🇷**) siguiendo el **Marco Común Europeo de Referencia para las Lenguas (MCER: A1, A2, B1, B2, C1 y C2)**.

---

## 2. Reglas de Gobernanza Obligatorias (Always-On)

1. **Protocolo de Consulta Previa (`approval-first-improvements`):**
   - **NUNCA** modifiques el código para implementar mejoras opcionales, nuevas funcionalidades, rediseños visuales o cambios arquitectónicos sin preguntar primero al usuario.
   - Ante cualquier auditoría o solicitud de mejoras, presenta primero el diagnóstico y las **opciones numeradas** (explicando qué hace cada una, su beneficio y su impacto) y espera la aprobación explícita del usuario.
   - Solo aplica cambios directos cuando el usuario pida explícitamente reparar un error/bug específico o cuando ya haya aprobado una opción propuesta.

2. **Estándar Pedagógico de Maestro Políglota (`polyglot-language-master`):**
   - **Prohibición de palabras aisladas sin contexto:** Ningún ejercicio de *Writing*, *Speaking*, *Listening* o *Mix* debe presentar monosílabos o fragmentos sueltos (como `"am"`, `"is"`, `"where"`). Toda práctica debe utilizar oraciones completas, naturales y con contexto comunicativo real (validadas por `PracticeSentenceFilter`).
   - **Progresión real por nivel (A1 → C2):** La complejidad gramatical y léxica debe crecer desde oraciones cotidianas de supervivencia en **A1** hasta registros académicos, profesionales, literarios y diplomáticos en **C1–C2**.
   - **Evaluación inteligente de voz y texto (`SpeechManager`):** El sistema debe aceptar contracciones naturales equivalentes (ej. `"she's"` = `"she is"`, `"I'm"` = `"I am"`, tanto con apóstrofe recto `'` como curvo `’`), tolerar 1 error tipográfico menor (*"¡Casi perfecto!"*) y entregar retroalimentación constructiva en español.
   - **Claridad en ejercicios de Escritura (`Writing` / `TRANSLATE_BUILD`):** Todo ejercicio de traducción escrita debe mostrar al estudiante una oración clara en español para traducir al idioma objetivo, nunca instrucciones vacías como *"Escucha el audio..."* o *"Toca el micrófono..."*.
   - **Pistas Pedagógicas Limitadas (Máximo 3 por sesión, sin regalar la respuesta):** Las pistas están limitadas a **3 por sesión** (máximo 1 por pregunta) y deben orientar al estudiante (mostrando estructura enmascarada, el primer bloque o descartando 1 distractor) sin revelar jamás la solución completa.
   - **Soporte Fonético, Flashcards y Diálogos Situacionales:** Todos los ejercicios auditivos, de pronunciación, el Glosario Interactivo (con modo **Flashcards `🃏`**) y los **Diálogos Situacionales (`☕ Situaciones de la Vida Real`)** cuentan con reproducción normal ($1.0\times$) y lenta (🐢 $0.65\times$), además de guía fonética contextual en ejercicios de *Speaking* y soporte Pinyin para Chino Mandarín.

---

## 3. Arquitectura del Sistema (Clean Architecture + MVVM)

El proyecto aplica **Modern Android Development (MAD)** con separación estricta de responsabilidades en 4 paquetes principales dentro de `app/src/main/java/com/linguaflow/app/`:

```text
com.linguaflow.app/
├── domain/                  # Capa de Dominio (Kotlin puro, reglas de negocio)
│   ├── model/               # Entidades de dominio (Language, Level, Lesson, Question, User)
│   ├── repository/          # Interfaces de repositorios (AuthRepository, LanguageRepository, ProgressRepository)
│   ├── usecase/             # Casos de uso (EvaluateAnswerUseCase, UnlockLevelUseCase, ProcessPlacementTestUseCase)
│   └── util/                # Filtros y validadores pedagógicos (PracticeSentenceFilter)
├── data/                    # Capa de Datos (Persistencia Offline-First con Room SQLite)
│   ├── local/               # LinguaFlowDatabase, SeedModels y sincronización desde seed_content.json
│   │   ├── dao/             # DAOs reactivos con Flow y suspend functions (Daos.kt)
│   │   └── entity/          # Entidades SQLite (@Entity) e índices relacionales
│   └── repository/          # Implementaciones concretas (RepositoriesImpl.kt)
├── ui/                      # Capa de Presentación (Jetpack Compose + Material 3 + ViewModels)
│   ├── navigation/          # Grafo de navegación (NavGraph.kt)
│   ├── screens/             # Pantallas por módulo (auth, home, path, lesson, practice, placement, profile)
│   └── theme/               # Sistema de diseño Indigo/Coral adaptativo Claro/Oscuro (Color.kt, Theme.kt, Type.kt)
└── util/                    # Motores nativos de voz, síntesis musical PCM y racha diaria (SpeechManager, SoundAndHapticsManager, DailyGoalAndStreakManager)
```

---

## 4. Banco Curricular (`seed_content.json`)

Ubicado en `app/src/main/assets/seed_content.json`:
- Contiene **1,404 preguntas** distribuidas en **4 idiomas** con **66 niveles cada uno** (subniveles `1..10` + `Examen` para cada etapa MCER `A1`–`C2`):
  - **Inglés 🇺🇸 (`en`, `en-US`):** 66 niveles, 540 preguntas.
  - **Italiano 🇮🇹 (`it`, `it-IT`):** 66 niveles, 300 preguntas.
  - **Chino Mandarín 🇨🇳 (`zh`, `zh-CN`):** 66 niveles, 300 preguntas (con Pinyin en `prompt_phonetic`).
  - **Portugués 🇧🇷 (`pt`, `pt-BR`):** 66 niveles, 264 preguntas.

---

## 5. Comandos de Compilación, Pruebas y Despliegue

Antes de confirmar cualquier cambio de código o subirlo a GitHub, el agente debe ejecutar y verificar:

1. **Ejecutar la suite de pruebas unitarias (JUnit 4):**
   ```powershell
   .\gradlew testDebugUnitTest
   ```
2. **Compilar el APK de depuración:**
   ```powershell
   .\gradlew assembleDebug
   ```
3. **Instalar en dispositivo físico por ADB (cuando esté conectado):**
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
   ```
