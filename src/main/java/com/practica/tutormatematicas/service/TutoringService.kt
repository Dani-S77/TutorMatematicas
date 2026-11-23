package com.practica.tutormatematicas.service

import com.practica.tutormatematicas.feedback.FeedbackGenerator
import com.practica.tutormatematicas.feedback.RuleBasedFeedbackGenerator
import com.practica.tutormatematicas.model.Exercise
import com.practica.tutormatematicas.model.Level
import com.practica.tutormatematicas.parser.AnswerParser
import com.practica.tutormatematicas.repository.ExerciseRepository
import com.practica.tutormatematicas.session.SessionPhase
import com.practica.tutormatematicas.session.StudentSession
import java.util.concurrent.ConcurrentHashMap

/** Resultado de comprobar una respuesta: lo que el controlador le devuelve al frontend. */
data class AnswerResult(
    val correct: Boolean,
    val message: String,
    val stepsDone: Int,
    val finished: Boolean,
    /** REGLAS, MODELO o RESPALDO: de dónde salió la explicación. */
    val source: String = "REGLAS"
)

/** Lo que el frontend necesita para dibujar la pantalla en cualquier momento. */
data class SessionView(
    val topicKey: String?,
    val level: Level?,
    val phaseName: String,
    val lessonTitle: String,
    val lessonSubtitle: String,
    val question: String?,
    val hint: String?,
    val stepsDone: Int,
    val totalSteps: Int
)

/**
 * PATRÓN: capa de Servicio (Service Layer).
 *
 * Esta clase es el "cerebro" del agente: decide qué pregunta sigue, si
 * una respuesta es correcta, y cuándo pasar de diagnóstico a práctica.
 * No sabe nada de HTTP ni de JSON (eso es trabajo del servidor) y no
 * sabe de dónde salen los ejercicios (eso es trabajo del Repository).
 * Esta separación es la misma que un @Service de Spring en Java:
 * Controller → Service → Repository.
 *
 * Las sesiones se guardan en memoria en un ConcurrentHashMap para que
 * varios estudiantes puedan usar el agente a la vez sin pisarse los
 * datos entre sí — el mismo cuidado que tendrías con un Map compartido
 * en un servidor Java.
 */
class TutoringService(
    private val repository: ExerciseRepository,
    private val answerParser: AnswerParser,
    private val feedbackGenerator: FeedbackGenerator = RuleBasedFeedbackGenerator()
) {
    private val sessions = ConcurrentHashMap<String, StudentSession>()
    private val stepsPerDiagnostic = 2
    private val stepsPerPractice = 4
    private val totalSteps = stepsPerDiagnostic + stepsPerPractice

    fun getOrCreateSession(id: String): StudentSession =
        sessions.computeIfAbsent(id) { StudentSession(it) }

    fun chooseTopic(session: StudentSession, topicKey: String): SessionView {
        val topic = repository.findByKey(topicKey)
            ?: throw IllegalArgumentException("Tema desconocido: $topicKey")

        session.phase = SessionPhase.Diagnostic(
            topicKey = topic.key,
            questions = topic.diagnostic,
            index = 0,
            correctCount = 0
        )
        session.totalStepsDone = 0
        return buildView(session)
    }

    fun submitAnswer(session: StudentSession, rawAnswer: String): AnswerResult {
        val exercise = session.currentExercise
            ?: throw IllegalStateException("No hay un ejercicio activo en esta sesión.")

        val parsed = answerParser.parse(rawAnswer)
            ?: return AnswerResult(
                correct = false,
                message = "No pude leer esa respuesta. Intenta con un número o una fracción como 3/4.",
                stepsDone = session.totalStepsDone,
                finished = false
            )

        val correct = Math.abs(parsed - exercise.answer) < 1e-6
        session.totalStepsDone += 1

        val feedback = feedbackGenerator.explain(exercise, rawAnswer, correct)
        val finished = advancePhase(session, correct)

        return AnswerResult(
            correct = correct,
            message = feedback.text,
            stepsDone = session.totalStepsDone,
            finished = finished,
            source = feedback.source.name
        )
    }

    /** Decide a qué fase pasar después de responder. Devuelve true si la ronda terminó. */
    private fun advancePhase(session: StudentSession, lastCorrect: Boolean): Boolean {
        val topic = repository.findByKey(sessionTopicKey(session))
            ?: throw IllegalStateException("Sesión sin tema asociado.")

        when (val phase = session.phase) {
            is SessionPhase.Diagnostic -> {
                val nextIndex = phase.index + 1
                val nextCorrect = phase.correctCount + if (lastCorrect) 1 else 0

                session.phase = if (nextIndex < phase.questions.size) {
                    phase.copy(index = nextIndex, correctCount = nextCorrect)
                } else {
                    val level = if (nextCorrect >= phase.questions.size) Level.INTERMEDIO else Level.BASICO
                    val bank = topic.exercisesByLevel[level].orEmpty()
                    SessionPhase.Practice(topicKey = phase.topicKey, level = level, queue = bank)
                }
                return false
            }
            is SessionPhase.Practice -> {
                val remaining = phase.queue.drop(1)
                session.phase = if (remaining.isNotEmpty()) {
                    phase.copy(queue = remaining)
                } else {
                    SessionPhase.Done(topicKey = phase.topicKey, level = phase.level)
                }
                return remaining.isEmpty()
            }
            else -> return true
        }
    }

    private fun sessionTopicKey(session: StudentSession): String = when (val p = session.phase) {
        is SessionPhase.Diagnostic -> p.topicKey
        is SessionPhase.Practice -> p.topicKey
        is SessionPhase.Done -> p.topicKey
        SessionPhase.Idle -> throw IllegalStateException("Sesión sin tema asociado.")
    }

    fun buildView(session: StudentSession): SessionView {
        return when (val phase = session.phase) {
            SessionPhase.Idle -> SessionView(
                topicKey = null, level = null, phaseName = "idle",
                lessonTitle = "Elige un tema para comenzar",
                lessonSubtitle = "Selecciona una de las opciones disponibles.",
                question = null, hint = null,
                stepsDone = 0, totalSteps = totalSteps
            )
            is SessionPhase.Diagnostic -> {
                val current = phase.questions[phase.index]
                SessionView(
                    topicKey = phase.topicKey, level = null, phaseName = "diagnostic",
                    lessonTitle = current.title, lessonSubtitle = current.subtitle,
                    question = current.question, hint = current.hint,
                    stepsDone = session.totalStepsDone, totalSteps = totalSteps
                )
            }
            is SessionPhase.Practice -> {
                val current = phase.queue.first()
                SessionView(
                    topicKey = phase.topicKey, level = phase.level, phaseName = "practice",
                    lessonTitle = current.title, lessonSubtitle = current.subtitle,
                    question = current.question, hint = current.hint,
                    stepsDone = session.totalStepsDone, totalSteps = totalSteps
                )
            }
            is SessionPhase.Done -> SessionView(
                topicKey = phase.topicKey, level = phase.level, phaseName = "done",
                lessonTitle = "Ronda completada",
                lessonSubtitle = "Nivel ${phase.level.name.lowercase()}",
                question = null, hint = null,
                stepsDone = session.totalStepsDone, totalSteps = totalSteps
            )
        }
    }

    /** Vuelve la sesión al estado inicial (sin tema elegido). */
    fun reset(session: StudentSession) {
        session.phase = SessionPhase.Idle
        session.totalStepsDone = 0
    }

    fun topics() = repository.findAll()
}
