# 🌍 LinguaFlow — Plataforma Móvil Políglota de Aprendizaje de Idiomas (MCER A1–C2)

**LinguaFlow** es una aplicación nativa para **Android** desarrollada en **Kotlin** con **Jetpack Compose**, **Clean Architecture (MVVM)** y persistencia local **Room SQLite**. Permite aprender y practicar **4 idiomas completos**: **Inglés 🇺🇸**, **Italiano 🇮🇹**, **Chino Mandarín 🇨🇳** (con soporte fonético Pinyin) y **Portugués 🇧🇷** desde el nivel **A1** hasta **C2** del Marco Común Europeo de Referencia para las Lenguas (MCER).

---

## ✨ Características Principales y Pedagógicas

1. **Ruta de Aprendizaje Progresiva MCER (A1 → C2) en 4 Idiomas:**
   - **264 niveles/subniveles** (66 por idioma: `A1.1–A1.10 + Examen A1` hasta `C2.1–C2.10 + Examen C2`) y **1,404 ejercicios interactivos** estructurados por competencias comunicativas.
   - **Miniguía Gramatical Previa (`📖 Con Guía`):** Antes de comenzar cada subnivel, el estudiante puede revisar los puntos clave, estructuras y ejemplos con audio nativo.
   - **Examen de Ubicación Inicial (Placement Test):** Evalúa el dominio previo del alumno en Inglés, Italiano, Chino o Portugués y desbloquea automáticamente los niveles correspondientes.

2. **Laboratorio de Entrenamiento por Habilidades (`PracticeHubScreen`):**
   - **🗣️ Speaking (Pronunciación In-App):** Reconocimiento de voz nativo integrado (`SpeechManager`) con evaluación fonética, aceptación inteligente de contracciones habladas (`"I'm"` = `"I am"`, `"she's"` = `"she is"`) y **Guía Fonética de Pronunciación** adaptada por idioma (tonos Pinyin en chino, consonantes dobles en italiano, vocales nasales `ão/õe` en portugués y enlaces en inglés).
   - **✍️ Writing (Escritura):** Traducción de oraciones completas en contexto con **tolerancia inteligente a errores de tipeo de 1 letra** (*"¡Casi perfecto!"*).
   - **🎧 Listening (Comprensión Auditiva):** Síntesis de voz a **velocidad normal ($1.0\times$)** y **velocidad lenta / tortuga (🐢 $0.65\times$)**.
   - **🔀 Mix de Habilidades:** Sesiones combinadas de habla, escucha y escritura.
   - **☕ Situaciones de la Vida Real (Simulador Conversacional Roleplay):** Diálogos interactivos por turnos en **Cafetería y Restaurante**, **Viajes y Aeropuerto** y **Entrevista y Trabajo** para los 4 idiomas con audio nativo y retroalimentación inmediata.
   - **📚 Glosario Interactivo y Modo Flashcards (`🃏 Flashcards`):** Explorador de vocabulario filtrado por subnivel (`A1.1`, `A1.2`, etc.) con **Vista Lista (`📋`)** y **Tarjetas de Memoria Giratorias (`🃏 Flashcards`)** para autoevaluación activa (*"¿Ya la dominabas? 👍 Ya la sé / 🔄 Repasar"*).
   - **🧠 Repaso Inteligente de Errores (SRS):** Tarjeta de repetición espaciada que registra los errores cometidos y permite dominarlos hasta dejar el contador en cero.

3. **Sistema de 3 Pistas Pedagógicas por Sesión:**
   - Límite estricto de **3 pistas por lección o práctica** (máximo 1 por pregunta).
   - Las pistas **orientan sin regalar jamás la solución completa**: muestran la estructura enmascarada de la oración (`I a_ a s______`), indican el bloque inicial en ejercicios de armado o descartan 1 distractor incorrecto en opción múltiple.

4. **Experiencia Multisensorial, Meta Diaria y Gamificación:**
   - **Sintetizador Musical Polifónico (`SoundAndHapticsManager`):** Acordes musicales alegres estilo marimba/chime generados en PCM a 44.1 kHz y vibración háptica al acertar, equivocarse o celebrar un nivel completado.
   - **Meta Diaria de Estudio y Racha Real (`DailyGoalAndStreakManager`):** Seguimiento diario de sesiones completadas (`3 sesiones/día`) y racha real consecutiva (`🔥 días`) en la pantalla de Inicio y en `Mi Perfil`.
   - **Panel de Estadísticas y Medallas (`🏆 Logros Políglotas`):** Seguimiento de racha activa (`🔥`), gemas XP (`💎`), subniveles completados y medallas desbloqueables.
   - **Modo Oscuro / Modo Claro Automático:** La paleta visual se adapta dinámicamente a la configuración del sistema Android (`isSystemInDarkTheme()`) y preserva el estado activo al girar la pantalla.

---

## 🏗️ Arquitectura del Proyecto

```text
com.linguaflow.app/
├── domain/                  # Reglas de negocio, modelos MCER, casos de uso y filtros pedagógicos
├── data/                    # Room Database SQLite, DAOs reactivos (Flow) y carga desde seed_content.json
├── ui/                      # Jetpack Compose, Material 3, Tema Oscuro/Claro reactivo y ViewModels
└── util/                    # SpeechManager, SoundAndHapticsManager, DailyGoalAndStreakManager y TTS
```

---

## 🧪 Ejecución de Pruebas y Compilación

```powershell
# Ejecutar pruebas unitarias (18 tests de integridad curricular, voz y filtros)
.\gradlew testDebugUnitTest

# Compilar APK Debug
.\gradlew assembleDebug
```

Consulta [`AGENTS.md`](AGENTS.md) para ver las reglas de gobernanza de agentes de IA (`approval-first-improvements` y `polyglot-language-master`).
