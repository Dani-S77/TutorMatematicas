package com.practica.tutormatematicas.session

import com.practica.tutormatematicas.model.Exercise


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
