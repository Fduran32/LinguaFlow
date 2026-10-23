package com.linguaflow.app.domain.util

/**
 * Filtro y sanitizador de oraciones para el Centro de Entrenamiento (Practice Hub).
 * Garantiza que en Writing, Speaking, Listening y Mix nunca se presenten monosílabos,
 * fragmentos gramaticales ("am", "is", "where") o plantillas con huecos ("___").
 */
object PracticeSentenceFilter {

    /**
     * Determina si un texto representa una oración completa, natural y pedagógicamente valiosa.
     */
    fun isMeaningfulSentence(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val clean = text.trim()

        // 1. Descartar ejercicios de rellenar huecos o incompletos
        if (clean.contains("___") || clean.contains("...") || clean.contains("_")) return false

        // 2. Descartar preguntas meta-lingüísticas en español (ej. "¿Cómo se dice...?", "¿Cuál es...?")
        if (clean.startsWith("¿") || clean.startsWith("?")) return false

        // 3. Contar palabras reales descartando signos de puntuación
        val words = clean.split(Regex("[\\s,;:.!?¿¡\"']+")).filter { it.isNotBlank() }

        // 4. Mínimo 2 palabras y al menos 8 caracteres para pares breves, o 3+ palabras
        // Esto descarta palabras únicas ("am", "is", "apple") y pares ultra-cortos ("I am", "to go", "at in")
        if (words.size < 2) return false
        if (words.size < 3 && clean.length < 8) return false

        return true
    }

    /**
     * Limpia los prefijos de instrucciones de las preguntas para dejar una oración fluida en español.
     * Ejemplo: "Ordena las fichas: 'Gusto en conocerte'" -> "Gusto en conocerte"
     * Ejemplo: "Pronuncia en voz alta: 'Ella bebe agua'" -> "Ella bebe agua"
     */
    fun cleanSpanishPromptText(prompt: String): String {
        return prompt
            .replace(
                Regex(
                    "^(Ordena( las fichas)?|Traduce( la siguiente frase)?( al inglés)?|Pronuncia( en voz alta)?):\\s*['\"]?",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .replace(Regex("['\"]$"), "")
            .trim()
    }
}
