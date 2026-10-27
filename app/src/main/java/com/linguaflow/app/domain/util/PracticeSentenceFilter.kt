package com.linguaflow.app.domain.util

/**
 * Filtro y sanitizador de oraciones para el Centro de Entrenamiento (Practice Hub).
 * Garantiza que en Writing, Speaking, Listening y Mix nunca se presenten monosílabos,
 * fragmentos gramaticales ("am", "is", "where"), plantillas con huecos ("___")
 * ni instrucciones meta-lingüísticas ("Escucha atentamente...", "Toca el micrófono...").
 */
object PracticeSentenceFilter {

    private val INSTRUCTIONAL_PREFIXES = listOf(
        "escucha ",
        "escucha:",
        "toca el micrófono",
        "toca el microfono",
        "pronuncia ",
        "pronuncia:",
        "selecciona ",
        "selecciona:",
        "completa ",
        "completa:",
        "elige ",
        "elige:",
        "lee ",
        "ordena ",
        "ordena:",
        "ordina ",
        "ordina:",
        "traduce ",
        "traduce:"
    )

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

        // 3. Descartar si el texto es una instrucción del sistema y no una frase del idioma
        val lower = clean.lowercase()
        if (INSTRUCTIONAL_PREFIXES.any { lower.startsWith(it) }) return false

        // 4. Contar palabras reales descartando signos de puntuación
        val words = clean.split(Regex("[\\s,;:.!?¿¡\"']+")).filter { it.isNotBlank() }

        // 5. Mínimo 2 palabras y al menos 8 caracteres para pares breves, o 3+ palabras
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
        val trimmed = prompt.trim()
        // Si contiene una frase entre comillas simples al final (ej. "Ordena las fichas: 'Gusto en conocerte'"), extraerla directamente
        val quotedMatch = Regex(":\\s*'(.+)'\\s*$").find(trimmed)
        if (quotedMatch != null) {
            return quotedMatch.groupValues[1].trim()
        }

        return trimmed
            .replace(
                Regex(
                    "^(Ordena( las fichas| los caracteres| las palabras)?|Ordina( le tessere| le parole)?|Traduce( la siguiente frase)?( al inglés)?|Pronuncia( en voz alta)?|Toca el micrófono[^:]*):\\s*['\"]?",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .replace(Regex("['\"]$"), "")
            .trim()
    }

    /**
     * Extrae y valida estrictamente que el enunciado sea una oración real en español apta para traducir en Writing.
     * Devuelve null si el enunciado es una instrucción de Listening/Speaking sin frase en español (ej. "Escucha atentamente..." o "Toca el micrófono y pronuncia:").
     */
    fun extractSpanishPromptForWriting(questionType: String, prompt: String, targetAnswer: String): String? {
        val rawPrompt = prompt.trim()
        if (rawPrompt.isBlank() || rawPrompt.contains("___")) return null

        // Solo se permiten ejercicios TRANSLATE_BUILD o aquellos que incluyan explícitamente la traducción entre comillas simples
        val hasQuotedSpanish = Regex(":\\s*'(.+)'\\s*$").containsMatchIn(rawPrompt)
        if (questionType != "TRANSLATE_BUILD" && !hasQuotedSpanish) {
            return null
        }

        val cleanSpanish = cleanSpanishPromptText(rawPrompt)
        if (cleanSpanish.isBlank()) return null

        val lowerSpanish = cleanSpanish.lowercase()
        // Rechazar cualquier instrucción residual ("Escucha...", "Toca el micrófono...", "Pronuncia...", etc.)
        if (INSTRUCTIONAL_PREFIXES.any { lowerSpanish.startsWith(it) } || lowerSpanish.endsWith(":")) {
            return null
        }

        // Rechazar si la frase extraída es igual a la respuesta en el idioma objetivo
        if (cleanSpanish.equals(targetAnswer.trim(), ignoreCase = true)) {
            return null
        }

        return cleanSpanish
    }
}
