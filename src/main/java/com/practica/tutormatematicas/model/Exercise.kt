package com.practica.tutormatematicas.model

/**
 * Nivel de dificultad al que pertenece un ejercicio.
 * Un enum es la forma idiomática en Kotlin (y en Java) de representar
 * un conjunto cerrado y conocido de valores.
 */
enum class Level {
    BASICO,
    INTERMEDIO
}

/**
 * Un ejercicio o pregunta de diagnóstico.
 *
 * Es una "data class": Kotlin genera automáticamente equals(), hashCode(),
 * toString() y copy() a partir de las propiedades del constructor — el
 * equivalente a lo que en Java escribirías a mano (o generabas con Lombok).
 *
 * @param question      Texto del ejercicio, ej. "1/2 + 1/4"
 * @param answer        Respuesta correcta como número (para poder comparar)
 * @param displayAnswer Respuesta correcta tal como se le muestra al estudiante, ej. "3/4"
 * @param title         Título corto de la habilidad que practica, ej. "Suma de fracciones"
 * @param subtitle      Aclaración breve, ej. "Mismo denominador"
 * @param explanation   Explicación del procedimiento (null en preguntas de diagnóstico)
 * @param hint          Pista corta para el estudiante
 */
data class Exercise(
    val question: String,
    val answer: Double,
    val displayAnswer: String,
    val title: String,
    val subtitle: String,
    val explanation: String? = null,
    val hint: String? = null
)
