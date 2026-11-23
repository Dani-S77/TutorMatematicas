package com.practica.tutormatematicas.feedback

import com.practica.tutormatematicas.model.Exercise

enum class FeedbackSource {
    REGLAS,
    MODELO,
    RESPALDO
}

data class Feedback(val text: String, val source: FeedbackSource)

interface FeedbackGenerator {
    fun explain(exercise: Exercise, studentAnswer: String, correct: Boolean): Feedback
}


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


interface ExplanationClient {
    fun explain(question: String, studentAnswer: String, correctAnswer: String, reference: String?): String
}


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
