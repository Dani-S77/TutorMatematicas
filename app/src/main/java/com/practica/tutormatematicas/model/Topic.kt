package com.practica.tutormatematicas.model


data class Topic(
    val key: String,
    val displayName: String,
    val diagnostic: List<Exercise>,
    val conceptByLevel: Map<Level, String>,
    val exercisesByLevel: Map<Level, List<Exercise>>
)
