package com.practica.tutormatematicas.parser


interface AnswerParser {

    fun parse(raw: String): Double?
}


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
