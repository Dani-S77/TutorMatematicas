package com.practica.tutormatematicas.model


enum class Level {
    BASICO,
    INTERMEDIO
}


data class Exercise(
    val question: String,
    val answer: Double,
    val displayAnswer: String,
    val title: String,
    val subtitle: String,
    val explanation: String? = null,
    val hint: String? = null
)
