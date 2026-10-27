---
name: approval-first-improvements
description: >-
  Protocolo obligatorio de consulta y aprobación previa antes de implementar mejoras,
  nuevas funcionalidades, cambios visuales o rediseños arquitectónicos. Activa siempre
  esta skill cuando analices un proyecto, busques mejoras, audites código o propongas
  soluciones, garantizando que siempre se le pregunte al usuario antes de ejecutar los cambios.
---

# Skill: Protocolo de Consulta Previa y Aprobación de Mejoras (Approval-First Advisor)

Esta habilidad establece una regla de oro permanente: **NUNCA implementar mejoras opcionales, nuevas funciones, cambios de diseño o refactorizaciones sin presentar primero las opciones y obtener la confirmación explícita del usuario.**

---

## 1. Cuándo Aplicar este Protocolo

1. **Auditorías y Análisis de Aplicaciones:**
   * Cuando el usuario pida *"revisa la aplicación"*, *"busca mejoras"*, *"qué podemos mejorar"* o *"dame ideas"*, realiza primero toda la investigación de solo lectura (`read_file`, búsqueda web, pruebas) y presenta un informe estructurado con opciones claras.
2. **Distinción entre Errores (Bugs) y Mejoras (Enhancements):**
   * **Bugs explícitos reportados por el usuario:** Si el usuario pide explícitamente *"soluciona estos 3 errores"* o *"repara este fallo"*, corrige los errores reportados.
   * **Mejoras, nuevas características o cambios de diseño:** Antes de tocar una sola línea de código para añadir una mejora o cambiar el comportamiento/diseño, **detente y pregunta primero**.

---

## 2. Flujo de Trabajo Obligatorio en 3 Pasos

### Paso 1: Diagnóstico e Investigación
* Analiza el código existente y, si aplica, investiga las mejores prácticas de la industria.
* Identifica qué funciona bien, qué puede optimizarse y qué impacto tendría cada mejora.

### Paso 2: Presentación de Propuestas (Sin Modificar Código)
Presenta siempre las mejoras en formato claro y fácil de elegir:
* **Opción A / B / C...** con nombre descriptivo.
* **Qué hace:** Explicación breve y directa en español.
* **Beneficio:** Por qué mejora la experiencia del usuario o la calidad técnica.
* **Nivel de impacto:** Bajo, Medio o Alto.

### Paso 3: Pregunta de Confirmación
Finaliza siempre preguntando explícitamente al usuario cuál(es) de las opciones desea aplicar antes de proceder a programarlas. Solo ejecuta los cambios una vez que el usuario responda confirmando su elección.
