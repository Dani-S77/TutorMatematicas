package com.practica.tutormatematicas.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.practica.tutormatematicas.BuildConfig
import com.practica.tutormatematicas.feedback.ExplanationValidator
import com.practica.tutormatematicas.feedback.GeminiExplanationClient
import com.practica.tutormatematicas.feedback.HybridFeedbackGenerator
import com.practica.tutormatematicas.feedback.RuleBasedFeedbackGenerator
import com.practica.tutormatematicas.model.Topic
import com.practica.tutormatematicas.parser.FlexibleAnswerParser
import com.practica.tutormatematicas.repository.InMemoryExerciseRepository
import com.practica.tutormatematicas.service.AnswerResult
import com.practica.tutormatematicas.service.SessionView
import com.practica.tutormatematicas.service.TutoringService
import java.util.concurrent.Executors

/**
 * Puente entre la pantalla (Compose) y la lógica (TutoringService).
 *
 * La explicación de una respuesta incorrecta se pide directamente a la API de Google Gemini.
 * La llamada a la API es una petición HTTPS de red, por eso se ejecuta en un hilo aparte.
 */
class TutorViewModel : ViewModel() {

    private val service = TutoringService(
        InMemoryExerciseRepository(),
        FlexibleAnswerParser(),
        HybridFeedbackGenerator(
            client = GeminiExplanationClient(apiKey = BuildConfig.GEMINI_API_KEY),
            validator = ExplanationValidator(),
            rules = RuleBasedFeedbackGenerator()
        )
    )
    private val session = service.getOrCreateSession("local")
    private val executor = Executors.newSingleThreadExecutor()

    val topics: List<Topic> = service.topics()

    var view: SessionView by mutableStateOf(service.buildView(session))
        private set
    var input: String by mutableStateOf("")
        private set
    var result: AnswerResult? by mutableStateOf(null)
        private set
    var notice: String? by mutableStateOf(null)
        private set
    var hint: String? by mutableStateOf(null)
        private set
    var loading: Boolean by mutableStateOf(false)
        private set
    private var pendingView: SessionView? by mutableStateOf(null)

    val stepsDone: Int
        get() = pendingView?.stepsDone ?: view.stepsDone

    val waitingForNext: Boolean
        get() = result != null

    val isActive: Boolean
        get() = view.phaseName == "diagnostic" || view.phaseName == "practice"

    fun selectTopic(topicKey: String) {
        if (loading) return
        view = service.chooseTopic(session, topicKey)
        clearExerciseState()
    }

    fun onInputChange(text: String) {
        input = text
        notice = null
    }

    fun submit() {
        if (!isActive || waitingForNext || loading || input.isBlank()) return
        val answer = input
        loading = true
        executor.execute {
            try {
                val before = session.totalStepsDone
                val r = service.submitAnswer(session, answer)
                if (r.stepsDone == before) {
                    // Respuesta ilegible: no avanza ni cuenta como intento.
                    notice = r.message
                } else {
                    notice = null
                    result = r
                    pendingView = service.buildView(session)
                }
            } catch (e: IllegalStateException) {
                // No hay ejercicio activo: no hacer nada.
            } finally {
                loading = false
            }
        }
    }

    fun next() {
        pendingView?.let { view = it }
        clearExerciseState()
    }

    fun showHint() {
        hint = view.hint
    }

    fun restart() {
        if (loading) return
        service.reset(session)
        view = service.buildView(session)
        clearExerciseState()
    }

    override fun onCleared() {
        executor.shutdown()
    }

    private fun clearExerciseState() {
        pendingView = null
        result = null
        notice = null
        hint = null
        input = ""
    }
}
