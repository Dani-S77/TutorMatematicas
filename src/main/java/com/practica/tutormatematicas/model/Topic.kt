package com.practica.tutormatematicas.model

/**
 * Un tema de estudio completo: fracciones, ecuaciones de primer grado, etc.
 *
 * Agrupa todo lo que el agente necesita sobre ese tema:
 * - las preguntas de diagnóstico inicial,
 * - la explicación del concepto para cada nivel,
 * - el banco de ejercicios de práctica por nivel.
 *
 * `Map<Level, List<Exercise>>` es el equivalente en Kotlin de un
 * `Map<Level, List<Exercise>>` de Java (misma idea, sintaxis más corta).
 */
data class Topic(
    val key: String,
    val displayName: String,
    val diagnostic: List<Exercise>,
    val conceptByLevel: Map<Level, String>,
    val exercisesByLevel: Map<Level, List<Exercise>>
)
