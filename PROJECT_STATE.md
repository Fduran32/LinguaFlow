# 📌 LinguaFlow - Estado del Proyecto y Bitácora de Continuidad

Este documento resume el estado técnico, funcional y de arquitectura de **LinguaFlow**, permitiendo retomar el desarrollo y las mejoras en cualquier sesión futura sin pérdida de contexto.

---

## 🧭 Ubicación y Repositorio
- **Ruta Local:** `C:\Users\felip\Desktop\Proyecto_App_Idiomas\LinguaFlow`
- **Repositorio Remoto en GitHub:** [Fduran32/LinguaFlow](https://github.com/Fduran32/LinguaFlow)
- **Rama Principal:** `main`

---

## 🏗️ Arquitectura y Tecnologías
- **Lenguaje:** Kotlin 1.9.x
- **Framework UI:** Jetpack Compose con Material 3 y Single-Activity Pattern (`MainActivity.kt`).
- **Patrón:** Clean Architecture (Domain, Data, UI) + MVVM con `StateFlow` y Coroutines.
- **Base de Datos Local:** Room Database (SQLite) versión **9**:
  - `UserEntity`: Cuentas locales con hash SHA-256 de contraseñas, avatares y fecha de registro.
  - `UserLevelProgressEntity`: Progreso compuesto por `(userId, levelId)`.
  - `UserMistakeEntity`: Cuaderno de errores compuesto por `(userId, questionId)`.
  - `UserLanguageProfileEntity`: Nivel MCER actual por usuario e idioma `(userId, languageId)`.
- **Síntesis de Voz:** `android.speech.tts.TextToSpeech` soportando `en-US`, `it-IT` y `zh-CN`.

---

## 🎯 Funcionalidades Implementadas y Verificadas

### 1. Sistema de Usuarios y Autenticación Segura
- **Login y Registro formal (`AuthScreen.kt`):**
  - Sin botones rápidos de cambio de usuario en la pantalla principal (enfoque en sesión personal y privada estilo Duolingo).
  - Validaciones estrictas en tiempo real:
    - Nombre de usuario $\ge 3$ caracteres.
    - Contraseña $\ge 6$ caracteres con al menos 1 mayúscula, minúsculas y carácter especial (`@`, `#`, etc.).
  - Checklist interactivo de seguridad con feedback visual inmediato.
- **Avatares Personalizados (`UserAvatar.kt`):**
  - Selección entre emojis predefinidos (`🦉`, `🦊`, `🦁`, `🐼`, `🚀`, `🎓`, `🐱`, `🎨`).
  - Carga de fotografía real desde la galería con almacenamiento en sandbox interno (`filesDir/avatars`).
- **Pantalla de Perfil (`ProfileScreen.kt`):**
  - Visualización del usuario, fecha de ingreso y nivel asignado en cada uno de los 3 idiomas.
  - Modal para editar perfil (nombre y avatar).
  - Cierre de sesión seguro con diálogo de confirmación.

### 2. Marco Común Europeo (MCER: A1 a C2) y 10 Subniveles por Nivel
- Cobertura completa de los 6 niveles para los 3 idiomas (Inglés, Italiano, Chino).
- Cada nivel consta de **10 subniveles progresivos** con dificultad creciente.
- El nivel 10 de cada bloque actúa como examen final integrador.
- Posibilidad de practicar y repasar niveles previos una vez aprobados.

### 3. Prueba Diagnóstica Adaptativa (`PlacementTestScreen.kt`)
- Prueba escalonada que evalúa desde A1 hacia niveles superiores.
- Clasifica al usuario directamente en el nivel correspondiente según su puntaje.

### 4. Cuaderno Inteligente de Errores (`MistakesReviewScreen.kt`)
- Registro de respuestas incorrectas con contador en la barra superior.
- Sesiones de repaso exclusivas para corregir y dominar errores anteriores.

---

## 📋 Pasos para Retomar en la Próxima Sesión

1. **Abrir en el IDE como Proyecto Activo:**
   - En Antigravity / VS Code / Android Studio: `File > Open Folder...` y seleccionar `C:\Users\felip\Desktop\Proyecto_App_Idiomas\LinguaFlow`.
2. **Compilar y Ejecutar:**
   ```powershell
   .\gradlew.bat assembleDebug
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 🚀 Ideas y Roadmap para Próximas Mejoras
- [ ] **Sistema de Rachas (Streak) y XP:** Contador de días consecutivos practicando y puntos ganados por lección.
- [ ] **Reconocimiento de Voz (Speech-to-Text):** Validar la pronunciación del usuario usando el micrófono del teléfono.
- [ ] **Modo Desafío Contrarreloj:** Sesiones rápidas de 60 segundos para repasar vocabulario acumulado.
- [ ] **Logros y Medallas:** Insignias desbloqueables ("Primer nivel A1 completado", "Sin errores en un examen", etc.).
