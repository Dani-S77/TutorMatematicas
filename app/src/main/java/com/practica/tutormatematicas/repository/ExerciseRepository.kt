package com.practica.tutormatematicas.repository

import com.practica.tutormatematicas.model.Exercise
import com.practica.tutormatematicas.model.Level
import com.practica.tutormatematicas.model.Topic

/**
 * PATRÓN: Repository.
 *
 * El resto del sistema (el servicio, el servidor HTTP) no sabe ni le
 * importa que estos datos vengan de una lista escrita a mano en Kotlin.
 * Solo conoce esta interfaz: findAll(), findByKey(topicKey).
 *
 * Esto es exactamente lo que harías en Java con una interfaz
 * `ExerciseRepository` implementada hoy en memoria y mañana con JDBC o
 * JPA contra SQLite/Postgres — el resto del código no cambiaría nada.
 */
interface ExerciseRepository {
    fun findAll(): List<Topic>
    fun findByKey(topicKey: String): Topic?
}

/**
 * Implementación en memoria: el mismo banco de ejercicios que tenía el
 * demo en JavaScript, ahora como datos fuertemente tipados en Kotlin.
 * El día que quieras guardar esto en una base de datos, solo escribes
 * una clase `SqliteExerciseRepository : ExerciseRepository` nueva —
 * nada más del sistema necesita enterarse del cambio.
 */
class InMemoryExerciseRepository : ExerciseRepository {

    private val topics: List<Topic> = listOf(
        Topic(
            key = "fracciones",
            displayName = "fracciones",
            diagnostic = listOf(
                Exercise(
                    question = "1/2 + 1/4",
                    answer = 0.75,
                    displayAnswer = "3/4",
                    title = "Diagnóstico",
                    subtitle = "Pregunta 1 de 2",
                    hint = "Los denominadores no son iguales: busca uno común antes de sumar."
                ),
                Exercise(
                    question = "Simplifica 6/8",
                    answer = 0.75,
                    displayAnswer = "3/4",
                    title = "Diagnóstico",
                    subtitle = "Pregunta 2 de 2",
                    hint = "Divide numerador y denominador entre su máximo común divisor."
                )
            ),
            conceptByLevel = mapOf(
                Level.BASICO to "Para sumar o restar fracciones con el mismo denominador, se operan " +
                    "solo los numeradores y el denominador se mantiene igual. Para simplificar, se " +
                    "divide numerador y denominador entre su máximo común divisor.",
                Level.INTERMEDIO to "Cuando los denominadores son distintos, primero se busca un " +
                    "denominador común. Para multiplicar fracciones se multiplican numeradores entre " +
                    "sí y denominadores entre sí; para dividir, se multiplica por el inverso de la " +
                    "segunda fracción."
            ),
            exercisesByLevel = mapOf(
                Level.BASICO to listOf(
                    Exercise(
                        "1/3 + 1/3", 2.0 / 3.0, "2/3",
                        "Suma de fracciones", "Mismo denominador",
                        "Los denominadores ya son iguales, así que se suman los numeradores: 1/3 + 1/3 = 2/3.",
                        "Como el denominador es el mismo, solo suma los numeradores."
                    ),
                    Exercise(
                        "2/5 + 1/5", 3.0 / 5.0, "3/5",
                        "Suma de fracciones", "Mismo denominador",
                        "Mismo denominador: se suman los numeradores, 2 + 1 = 3, y queda 3/5.",
                        "El denominador no cambia; solo suma 2 + 1."
                    ),
                    Exercise(
                        "Simplifica 4/10", 2.0 / 5.0, "2/5",
                        "Simplificar fracciones", "Máximo común divisor",
                        "El máximo común divisor de 4 y 10 es 2. Al dividir ambos entre 2 queda 2/5.",
                        "¿Entre qué número puedes dividir 4 y 10 al mismo tiempo?"
                    ),
                    Exercise(
                        "1/2 − 1/4", 0.25, "1/4",
                        "Resta de fracciones", "Denominador común",
                        "Se convierte 1/2 en cuartos: 1/2 = 2/4. Luego 2/4 − 1/4 = 1/4.",
                        "Convierte 1/2 a cuartos antes de restar."
                    )
                ),
                Level.INTERMEDIO to listOf(
                    Exercise(
                        "1/2 + 1/3", 5.0 / 6.0, "5/6",
                        "Suma de fracciones", "Denominador distinto",
                        "El denominador común de 2 y 3 es 6: 1/2 = 3/6 y 1/3 = 2/6. Sumando: 3/6 + 2/6 = 5/6.",
                        "Busca un denominador común múltiplo de 2 y 3."
                    ),
                    Exercise(
                        "3/4 − 1/6", 7.0 / 12.0, "7/12",
                        "Resta de fracciones", "Denominador distinto",
                        "El denominador común de 4 y 6 es 12: 3/4 = 9/12 y 1/6 = 2/12. Restando: 9/12 − 2/12 = 7/12.",
                        "El denominador común de 4 y 6 es 12."
                    ),
                    Exercise(
                        "2/3 × 3/4", 0.5, "1/2",
                        "Multiplicación de fracciones", "Numerador por numerador",
                        "Se multiplica numerador por numerador y denominador por denominador: (2×3)/(3×4) = 6/12, que simplificado es 1/2.",
                        "Multiplica los numeradores entre sí y los denominadores entre sí."
                    ),
                    Exercise(
                        "5/6 ÷ 1/3", 2.5, "5/2",
                        "División de fracciones", "Multiplicar por el inverso",
                        "Dividir entre una fracción equivale a multiplicar por su inverso: 5/6 × 3/1 = 15/6 = 5/2.",
                        "Invierte la segunda fracción y multiplica."
                    )
                )
            )
        ),
        Topic(
            key = "ecuaciones",
            displayName = "ecuaciones de primer grado",
            diagnostic = listOf(
                Exercise(
                    "x + 3 = 7", 4.0, "4",
                    "Diagnóstico", "Pregunta 1 de 2",
                    hint = "Resta 3 en ambos lados del signo igual."
                ),
                Exercise(
                    "2x = 10", 5.0, "5",
                    "Diagnóstico", "Pregunta 2 de 2",
                    hint = "Divide ambos lados entre 2."
                )
            ),
            conceptByLevel = mapOf(
                Level.BASICO to "Resolver una ecuación de primer grado significa dejar la incógnita " +
                    "sola en un lado del igual. Lo que se hace a un lado, se hace también al otro: si " +
                    "algo suma, se resta en ambos lados; si algo multiplica, se divide en ambos lados.",
                Level.INTERMEDIO to "Cuando hay varios términos, primero se simplifica cada lado, y " +
                    "luego se despeja la incógnita moviendo los términos numéricos a un lado y los " +
                    "términos con x al otro."
            ),
            exercisesByLevel = mapOf(
                Level.BASICO to listOf(
                    Exercise(
                        "x + 5 = 9", 4.0, "4",
                        "Ecuaciones simples", "Despejar restando",
                        "Se resta 5 en ambos lados: x = 9 − 5 = 4.",
                        "Resta 5 en ambos lados del igual."
                    ),
                    Exercise(
                        "x − 2 = 6", 8.0, "8",
                        "Ecuaciones simples", "Despejar sumando",
                        "Se suma 2 en ambos lados: x = 6 + 2 = 8.",
                        "Suma 2 en ambos lados del igual."
                    ),
                    Exercise(
                        "3x = 12", 4.0, "4",
                        "Ecuaciones simples", "Despejar dividiendo",
                        "Se divide entre 3 en ambos lados: x = 12 ÷ 3 = 4.",
                        "Divide ambos lados entre 3."
                    ),
                    Exercise(
                        "x / 2 = 5", 10.0, "10",
                        "Ecuaciones simples", "Despejar multiplicando",
                        "Se multiplica por 2 en ambos lados: x = 5 × 2 = 10.",
                        "Multiplica ambos lados por 2."
                    )
                ),
                Level.INTERMEDIO to listOf(
                    Exercise(
                        "2x + 3 = 11", 4.0, "4",
                        "Ecuaciones con dos pasos", "Despejar en dos operaciones",
                        "Se resta 3 en ambos lados: 2x = 8. Luego se divide entre 2: x = 4.",
                        "Primero resta 3, luego divide entre 2."
                    ),
                    Exercise(
                        "3x − 4 = 11", 5.0, "5",
                        "Ecuaciones con dos pasos", "Despejar en dos operaciones",
                        "Se suma 4 en ambos lados: 3x = 15. Luego se divide entre 3: x = 5.",
                        "Primero suma 4, luego divide entre 3."
                    ),
                    Exercise(
                        "2(x + 1) = 10", 4.0, "4",
                        "Ecuaciones con paréntesis", "Propiedad distributiva",
                        "Se divide entre 2 en ambos lados: x + 1 = 5. Luego se resta 1: x = 4.",
                        "Puedes dividir todo entre 2 antes de distribuir."
                    ),
                    Exercise(
                        "5x − 3 = 2x + 9", 4.0, "4",
                        "Ecuaciones con x en ambos lados", "Agrupar términos semejantes",
                        "Se agrupan los términos con x a la izquierda y los números a la derecha: 5x − 2x = 9 + 3 → 3x = 12 → x = 4.",
                        "Resta 2x en ambos lados para dejar la x de un solo lado."
                    )
                )
            )
        )
    )

    override fun findAll(): List<Topic> = topics

    override fun findByKey(topicKey: String): Topic? = topics.find { it.key == topicKey }
}
