package com.practica.tutormatematicas.session

import com.practica.tutormatematicas.model.Exercise

/**
 * Representa la conversación de UN estudiante con el agente.
 *
 * En el demo en JavaScript esto vivía en una sola variable `state` en
 * el navegador. Aquí vive en el servidor, identificada por un `id`,
 * porque ahora el backend puede atender a varios estudiantes al mismo
 * tiempo — cada uno con su propia sesión independiente.
 */
class StudentSession(val id: String) {
    var phase: SessionPhase = SessionPhase.Idle
        internal set

    var totalStepsDone: Int = 0
        internal set

    /** El ejercicio que el estudiante está respondiendo ahora mismo, si lo hay. */
    val currentExercise: Exercise?
        get() = when (val p = phase) {
            is SessionPhase.Diagnostic -> p.questions.getOrNull(p.index)
            is SessionPhase.Practice -> p.queue.firstOrNull()
            else -> null
        }
}
