package com.practica.tutormatematicas.feedback

import com.practica.tutormatematicas.model.Exercise

/** De dónde salió la explicación que ve el estudiante. */
enum class FeedbackSource {
    /** Explicación fija escrita en el banco de ejercicios. */
    REGLAS,
    /** Explicación generada por el modelo de lenguaje y validada. */
    MODELO,
    /** El modelo falló o su respuesta no pasó la validación; se usó la explicación fija. */
    RESPALDO
}

data class Feedback(val text: String, val source: FeedbackSource)

/**
 * PATRÓN: Strategy. Decide CÓMO se explica una respuesta, pero nunca si es correcta:
 * el veredicto (correct) llega ya calculated por el código determinista.
 */
interface FeedbackGenerator {
    fun explain(exercise: Exercise, studentAnswer: String, correct: Boolean): Feedback
}

/** Explicaciones fijas del banco de ejercicios (la versión que ya existía). */
class RuleBasedFeedbackGenerator : FeedbackGenerator {
    override fun explain(exercise: Exercise, studentAnswer: String, correct: Boolean): Feedback {
        val explanation = exercise.explanation
        val text = when {
            correct && explanation != null -> explanation
            correct -> "La respuesta es ${exercise.displayAnswer}."
            explanation != null -> "$explanation La respuesta correcta es ${exercise.displayAnswer}."
            else -> "La respuesta correcta era ${exercise.displayAnswer}."
        }
        return Feedback(text, FeedbackSource.REGLAS)
    }
}

/**
 * Cliente de un servicio que genera explicaciones con un modelo de lenguaje.
 * Lanza una excepción si el servicio falla, no responde a tiempo o devuelve un error.
 */
interface ExplanationClient {
    fun explain(question: String, studentAnswer: String, correctAnswer: String, reference: String?): String
}

/**
 * Valida el texto del modelo ANTES de mostrarlo. Devuelve el texto limpio o null si debe descartarse.
 * Reglas: no vacío, longitud máxima, no declara correcta una respuesta incorrecta y, si menciona
 * "la respuesta correcta es X", X debe coincidir con la respuesta esperada.
 */
class ExplanationValidator(private val maxLength: Int = 700) {

    private val claimsCorrect = Regex(
        "(tu respuesta es correcta|respuesta correcta,? ¡?muy bien|¡correcto|¡muy bien|\\bcorrecto!|es correcta tu respuesta)",
        RegexOption.IGNORE_CASE
    )
    private val statedAnswer = Regex(
        "respuesta correcta (?:es|era)\\s*[:=]?\\s*(-?[0-9]+(?:[.,/][0-9]+)?)",
        RegexOption.IGNORE_CASE
    )

    fun validate(raw: String?, expectedAnswer: String): String? {
        if (raw == null) return null
        val text = raw.replace("**", "").replace("#", "").replace(Regex("\\s+"), " ").trim()
        if (text.isEmpty() || text.length > maxLength) return null
        if (claimsCorrect.containsMatchIn(text)) return null
        for (m in statedAnswer.findAll(text)) {
            val stated = m.groupValues[1].trimEnd('.', ',')
            if (stated != expectedAnswer) return null
        }
        return text
    }
}

/**
 * Combina el modelo de lenguaje con las reglas fijas.
 * - Respuesta correcta: no se llama al modelo (ahorra cuota y tiempo).
 * - Respuesta incorrecta: se pide la explicación al modelo, se valida y se agrega siempre la
 *   respuesta correcta desde el código. Si algo falla, se usa la explicación fija (RESPALDO).
 */
class HybridFeedbackGenerator(
    private val client: ExplanationClient,
    private val validator: ExplanationValidator = ExplanationValidator(),
    private val rules: FeedbackGenerator = RuleBasedFeedbackGenerator(),
    private val onEvent: (String) -> Unit = {}
) : FeedbackGenerator {

    override fun explain(exercise: Exercise, studentAnswer: String, correct: Boolean): Feedback {
        val fixed = rules.explain(exercise, studentAnswer, correct)
        if (correct) return fixed
        return try {
            val raw = client.explain(exercise.question, studentAnswer, exercise.displayAnswer, exercise.explanation)
            val clean = validator.validate(raw, exercise.displayAnswer)
            if (clean == null) {
                onEvent("validacion_rechazada")
                Feedback(fixed.text + " (Respuesta del modelo no pasó validación)", FeedbackSource.RESPALDO)
            } else {
                onEvent("modelo_ok")
                Feedback("$clean La respuesta correcta es ${exercise.displayAnswer}.", FeedbackSource.MODELO)
            }
        } catch (e: Exception) {
            android.util.Log.e("TutorAI", "Error llamando a Gemini", e)
            onEvent("modelo_fallo: " + (e.message ?: e.javaClass.simpleName))
            val detail = e.message?.take(150) ?: e.javaClass.simpleName
            Feedback("${fixed.text}\n[Error de Gemini: $detail]", FeedbackSource.RESPALDO)
        }
    }
}
