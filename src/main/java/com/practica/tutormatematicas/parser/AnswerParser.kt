package com.practica.tutormatematicas.parser

/**
 * PATRÓN: Strategy.
 *
 * Definimos el "qué" (convertir un texto en un número comparable) como
 * una interfaz, separado del "cómo". Hoy solo tenemos una forma de
 * interpretar respuestas, pero si mañana quisieras aceptar notación
 * científica o números romanos, agregarías otra clase que implemente
 * esta misma interfaz, sin tocar el resto del sistema.
 *
 * Es el mismo patrón que usarías en Java con una interfaz
 * `AnswerParser` y varias implementaciones intercambiables.
 */
interface AnswerParser {
    /** Devuelve el valor numérico del texto, o null si no se pudo interpretar. */
    fun parse(raw: String): Double?
}

/**
 * Interpreta números simples, fracciones ("3/4"), números mixtos
 * ("2 1/2") y respuestas de ecuaciones ("x=4"), tal como lo hacía la
 * función parseAnswer() del demo en JavaScript.
 */
class FlexibleAnswerParser : AnswerParser {

    private val mixedNumberRegex = Regex("""^(-?\d+)\s+(\d+)/(\d+)$""")
    private val xEqualsPrefix = Regex("""^x\s*=\s*""")

    override fun parse(raw: String): Double? {
        val cleaned = raw.trim()
            .lowercase()
            .replace(xEqualsPrefix, "")
            .replace(',', '.')

        if (cleaned.isEmpty()) return null

        mixedNumberRegex.find(cleaned)?.let { match ->
            val (wholeStr, numStr, denStr) = match.destructured
            val whole: Double = wholeStr.toDoubleOrNull() ?: return null
            val num: Double = numStr.toDoubleOrNull() ?: return null
            val den: Double = denStr.toDoubleOrNull() ?: return null
            if (den == 0.0) return null
            val sign = if (whole < 0) -1.0 else 1.0
            return whole + sign * (num / den)
        }

        if (cleaned.contains('/')) {
            val parts = cleaned.split('/')
            if (parts.size != 2) return null
            val num: Double = parts[0].toDoubleOrNull() ?: return null
            val den: Double = parts[1].toDoubleOrNull() ?: return null
            if (den == 0.0) return null
            return num / den
        }

        return cleaned.toDoubleOrNull()
    }
}
