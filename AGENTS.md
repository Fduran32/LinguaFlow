# AGENTS.md — Guía de Arquitectura, Pedagogía y Reglas para Agentes de IA

Este documento define las directrices técnicas, arquitectónicas y pedagógicas que cualquier Agente de IA (Google Antigravity, GitHub Copilot, Cursor, Claude Code, etc.) y desarrollador debe cumplir al trabajar en el repositorio **LinguaFlow**.

---

## 1. Descripción General del Proyecto

**LinguaFlow** es una aplicación móvil nativa para **Android** desarrollada en **Kotlin** con **Jetpack Compose**, diseñada para el aprendizaje estructurado y progresivo de múltiples idiomas (**Inglés 🇺🇸**, **Italiano 🇮🇹** y **Chino Mandarín 🇨🇳**) siguiendo el **Marco Común Europeo de Referencia para las Lenguas (MCER: A1, A2, B1, B2, C1 y C2)**.

---

## 2. Reglas de Gobernanza Obligatorias (Always-On)

1. **Protocolo de Consulta Previa (`approval-first-improvements`):**
   - **NUNCA** modifiques el código para implementar mejoras opcionales, nuevas funcionalidades, rediseños visuales o cambios arquitectónicos sin preguntar primero al usuario.
   - Ante cualquier auditoría o solicitud de mejoras, presenta primero el diagnóstico y las **opciones numeradas** (explicando qué hace cada una, su beneficio y su impacto) y espera la aprobación explícita del usuario.
   - Solo aplica cambios directos cuando el usuario pida explícitamente reparar un error/bug específico o cuando ya haya aprobado una opción propuesta.

2. **Estándar Pedagógico de Maestro Políglota (`polyglot-language-master`):**
   - **Prohibición de palabras aisladas sin contexto:** Ningún ejercicio de *Writing*, *Speaking*, *Listening* o *Mix* debe presentar monosílabos o fragmentos sueltos (como `"am"`, `"is"`, `"where"`). Toda práctica debe utilizar oraciones completas, naturales y con contexto comunicativo real (validadas por `PracticeSentenceFilter`).
   - **Progresión real por nivel (A1 → C2):** La complejidad gramatical y léxica debe crecer desde oraciones cotidianas de supervivencia en **A1** hasta registros académicos, profesionales, literarios y diplomáticos en **C1–C2**.
   - **Evaluación inteligente de voz y texto (`SpeechManager`):** El sistema debe aceptar contracciones naturales equivalentes (ej. `"she's"` = `"she is"`, `"I'm"` = `"I am"`, tanto con apóstrofe recto `'` como curvo `’`) y entregar retroalimentación constructiva en español.
   - **Claridad en ejercicios de Escritura (`Writing` / `TRANSLATE_BUILD`):** Todo ejercicio de traducción escrita debe mostrar al estudiante una oración clara en español para traducir al idioma objetivo, nunca instrucciones vacías como *"Escucha el audio..."* o *"Toca el micrófono..."*.

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
│   ├── screens/             # Pantallas por módulo (auth, home, path, lesson, practice, placement)
│   └── theme/               # Sistema de diseño Indigo/Coral (Color.kt, Theme.kt, Type.kt)
└── util/                    # Motores nativos de voz y audio (TtsManager.kt, SpeechManager.kt)
```

### Directrices Técnicas de Android (`senior-android-engineer`)
- **Unidirectional Data Flow (UDF):** Los `ViewModel` exponen estado inmutable mediante `StateFlow<UiState>` y reciben eventos desde la UI.
- **Persistencia Segura en Room:** Al actualizar el banco de preguntas desde `assets/seed_content.json` en `LinguaFlowDatabase`, se deben limpiar preguntas huérfanas (`deleteAllQuestions()` y `deleteOrphanMistakes()`) sin borrar las cuentas de usuario ni su progreso de niveles (`user_level_progress`).
- **Desbloqueo de Niveles:** Usar `saveLevelProgress` (`OnConflictStrategy.REPLACE`) en `ProgressRepositoryImpl` para garantizar que completar un subnivel ($\ge 70\%$) siempre persista y desbloquee el siguiente subnivel en orden secuencial (`orderIndex`). Solo el Subnivel 1 del bloque `A1` está desbloqueado por defecto al iniciar de cero.

---

## 4. Banco Curricular (`seed_content.json`)

Ubicado en `app/src/main/assets/seed_content.json`:
- Contiene **1,140 preguntas** distribuidas en **Inglés** (540 preguntas, 54 subniveles A1–C2), **Italiano** (300 preguntas, 30 subniveles A1–C2) y **Chino Mandarín** (300 preguntas, 30 subniveles A1–C2 con soporte de Pinyin en `prompt_phonetic`).
- **Tipos de preguntas soportados (`QuestionType`):**
  - `MULTIPLE_CHOICE`: Selección múltiple conceptual y situacional.
  - `FILL_BLANK`: Completar la estructura gramatical en contexto.
  - `TRANSLATE_BUILD`: Construcción de oraciones a partir de su traducción en español (`Ordena las fichas: '<oración en español>'`).
  - `LISTENING` / `LISTENING_MC`: Comprensión auditiva con síntesis de voz nativa (`TtsManager`).
  - `SPEAKING`: Producción oral evaluada con reconocimiento de voz (`SpeechManager`) y algoritmos de similitud fonética/léxica.

---

## 5. Comandos de Compilación, Pruebas y Despliegue

Antes de confirmar cualquier cambio de código o subirlo a GitHub, el agente debe ejecutar y verificar:

1. **Ejecutar la suite de pruebas unitarias (JUnit 4):**
   ```powershell
   .\gradlew testDebugUnitTest
   ```
   *Pruebas obligatorias incluidas en `app/src/test/java/com/linguaflow/app/`:*
   - `PracticeSentenceFilterTest`: Valida el rechazo de palabras sueltas (`"am"`), fragmentos incompletos e instrucciones filtradas en *Writing*.
   - `SpeechManagerTest`: Valida el cálculo de similitud fonética, expansión de contracciones (`"she's my mother"` vs `"She is my mother"`) y `EvaluateAnswerUseCase`.
   - `SeedContentIntegrityTest`: Audita las 1,140 preguntas de `seed_content.json`, verificando códigos MCER (`A1`–`C2`), tipos válidos de `QuestionType` y traducciones en español en todos los ejercicios `TRANSLATE_BUILD`.

2. **Compilar el APK de depuración:**
   ```powershell
   .\gradlew assembleDebug
   ```

3. **Instalar en dispositivo físico por ADB (cuando esté conectado):**
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 6. Skills Personalizadas del Proyecto

Este repositorio incluye definiciones de *Skills* en `.agents/skills/` para mantener la consistencia entre sesiones y equipos:
- [`.agents/skills/approval-first-improvements/SKILL.md`](.agents/skills/approval-first-improvements/SKILL.md): Protocolo de consulta y aprobación previa antes de aplicar mejoras.
- [`.agents/skills/polyglot-language-master/SKILL.md`](.agents/skills/polyglot-language-master/SKILL.md): Maestro Políglota y experto en diseño instruccional de idiomas (MCER A1–C2).
