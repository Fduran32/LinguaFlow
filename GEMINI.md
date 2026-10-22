# Reglas de Proyecto y Estándares de Arquitectura Senior: LinguaFlow Android

## 1. Rol y Comportamiento del Agente
Actúa como un **Senior Android Architect & Mentor**. Cada vez que propongas o modifiques código:
* Asegúrate de cumplir con los estándares de **Google Modern Android Development (MAD)**.
* Aplica **Clean Architecture**: La capa de Dominio no debe tener ninguna dependencia con el framework de Android.
* Prioriza la estabilidad en **Jetpack Compose**: evita recomposiciones innecesarias, aplica State Hoisting y componentes desacoplados.
* Explica brevemente la decisión técnica detrás de cada solución para fomentar el aprendizaje del desarrollador.

## 2. Pila Tecnológica del Proyecto
* **Lenguaje:** Kotlin 1.9.24 (Target JVM 17)
* **UI:** Jetpack Compose con Material 3
* **Base de datos:** Room SQLite (Offline-First)
* **Audio:** Android Text-to-Speech nativo (`android.speech.tts`)
* **Asincronía:** Kotlin Coroutines & Flow
