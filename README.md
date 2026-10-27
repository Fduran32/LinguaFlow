# 🌍 LinguaFlow — Plataforma Móvil Políglota de Aprendizaje de Idiomas (MCER A1–C2)

**LinguaFlow** es una aplicación nativa para **Android** desarrollada en **Kotlin** con **Jetpack Compose**, **Clean Architecture (MVVM)** y persistencia local **Room SQLite**. Permite aprender y practicar **Inglés 🇺🇸**, **Italiano 🇮🇹** y **Chino Mandarín 🇨🇳** (con soporte fonético Pinyin) desde el nivel **A1** hasta **C2** del Marco Común Europeo de Referencia para las Lenguas (MCER).

---

## ✨ Características Principales y Pedagógicas

1. **Ruta de Aprendizaje Progresiva MCER (A1 → C2):**
   - 114 subniveles y **1,140 ejercicios interactivos** estructurados por competencias comunicativas.
   - **Miniguía Gramatical Previa (`📖 Con Guía`):** Antes de comenzar cada subnivel, el estudiante puede revisar los puntos clave, estructuras y ejemplos con audio nativo.
   - **Examen de Ubicación Inicial (Placement Test):** Evalúa el dominio previo del alumno y desbloquea automáticamente los niveles correspondientes.

2. **Laboratorio de Entrenamiento por Habilidades (`PracticeHubScreen`):**
   - **🗣️ Speaking (Pronunciación In-App):** Reconocimiento de voz nativo integrado (`SpeechManager`) con evaluación fonética, aceptación inteligente de contracciones habladas (`"I'm"` = `"I am"`, `"she's"` = `"she is"`) y **Guía Fonética de Pronunciación** adaptada por idioma (tonos Pinyin en chino, consonantes dobles en italiano y enlaces en inglés).
   - **✍️ Writing (Escritura):** Traducción de oraciones completas en contexto con **tolerancia inteligente a errores de tipeo de 1 letra** (*"¡Casi perfecto!"*).
   - **🎧 Listening (Comprensión Auditiva):** Síntesis de voz a **velocidad normal ($1.0\times$)** y **velocidad lenta / tortuga (🐢 $0.65\times$)**.
   - **🔀 Mix de Habilidades:** Sesiones combinadas de habla, escucha y escritura.
   - **🧠 Repaso Inteligente de Errores (SRS):** Tarjeta de repetición espaciada que registra los errores cometidos y permite dominarlos hasta dejar el contador en cero.
   - **📚 Glosario Interactivo y Flashcards con Audio:** Explorador de vocabulario y frases clave filtrado por subnivel (`A1.1`, `A1.2`, etc.) con reproducción de audio normal ($1.0\times$) y lento ($0.65\times$).

3. **Sistema de 3 Pistas Pedagógicas por Sesión:**
   - Límite estricto de **3 pistas por lección o práctica** (máximo 1 por pregunta).
   - Las pistas **orientan sin regalar jamás la solución completa**: muestran la estructura enmascarada de la oración (`I a_ a s______`), indican el bloque inicial en ejercicios de armado o descartan 1 distractor incorrecto en opción múltiple.

4. **Experiencia Multisensorial, Gamificación y Tema Adaptativo:**
   - **Efectos de Sonido Nativos y Vibración Háptica (`SoundAndHapticsManager`):** Respuesta sonora y táctil inmediata al acertar, equivocarse o superar un nivel.
   - **Panel de Estadísticas y Medallas (`🏆 Logros Políglotas`):** Seguimiento de racha activa (`🔥`), gemas XP (`💎`), subniveles completados y medallas desbloqueables en `Mi Perfil`.
   - **Modo Oscuro / Modo Claro Automático:** La paleta visual se adapta dinámicamente a la configuración del sistema Android (`isSystemInDarkTheme()`) y preserva el estado activo al girar la pantalla.

---

## 🏗️ Arquitectura del Proyecto

```text
com.linguaflow.app/
├── domain/                  # Reglas de negocio, modelos MCER, casos de uso y filtros pedagógicos
├── data/                    # Room Database SQLite, DAOs reactivos (Flow) y carga desde seed_content.json
├── ui/                      # Jetpack Compose, Material 3, Tema Oscuro/Claro reactivo y ViewModels
└── util/                    # SpeechManager, SoundAndHapticsManager y síntesis de voz TTS
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
