package com.practica.tutormatematicas.session

import com.practica.tutormatematicas.model.Exercise
import com.practica.tutormatematicas.model.Level

/**
 * PATRÓN: State, usando una "sealed class".
 *
 * En el demo en JavaScript, el momento de la conversación se guardaba
 * en un texto: state.phase = 'diagnostic' | 'practice' | 'done'. Eso
 * funciona, pero nada impide escribir mal el texto, y el compilador no
 * te avisa si olvidas manejar un caso.
 *
 * Una "sealed class" en Kotlin es una jerarquía cerrada y conocida de
 * tipos: el compilador SÍ sabe cuáles son todos los posibles estados,
 * y un `when` sobre un SessionPhase te obliga a cubrirlos todos (o te
 * marca error). Es el equivalente a un enum de Java, pero donde cada
 * "valor" puede llevar sus propios datos.
 */
sealed class SessionPhase {

    /** Aún no se eligió tema. */
    object Idle : SessionPhase()

    /** Resolviendo las preguntas de diagnóstico de un tema. */
    data class Diagnostic(
        val topicKey: String,
        val questions: List<Exercise>,
        val index: Int,
        val correctCount: Int
    ) : SessionPhase()

    /** Practicando ejercicios de un tema, ya en un nivel definido. */
    data class Practice(
        val topicKey: String,
        val level: Level,
        val queue: List<Exercise>
    ) : SessionPhase()

    /** Ronda de práctica terminada. */
    data class Done(
        val topicKey: String,
        val level: Level
    ) : SessionPhase()
}
