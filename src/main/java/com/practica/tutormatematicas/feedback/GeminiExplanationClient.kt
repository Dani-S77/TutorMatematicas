package com.practica.tutormatematicas.feedback

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente directo para conectarse a la API de Google Gemini (generativelanguage.googleapis.com).
 * Prueba automáticamente los modelos disponibles (gemini-1.5-flash, gemini-2.0-flash) y maneja alta demanda (503).
 */
class GeminiExplanationClient(
    private val apiKey: String,
    private val connectTimeoutMs: Int = 5000,
    private val readTimeoutMs: Int = 20000
) : ExplanationClient {

    private val candidateModels = listOf(
        "gemini-1.5-flash" to "v1beta",
        "gemini-2.0-flash" to "v1beta",
        "gemini-1.5-flash" to "v1",
        "gemini-2.5-flash" to "v1beta",
        "gemini-1.5-pro" to "v1beta"
    )

    override fun explain(question: String, studentAnswer: String, correctAnswer: String, reference: String?): String {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank() || trimmedKey == "PONE_AQUI_TU_API_KEY" || trimmedKey == "TU_API_KEY_AQUI") {
            throw IOException("La API Key de Gemini no está configurada en local.properties.")
        }

        if (!trimmedKey.startsWith("AIzaSy")) {
            throw IOException("La clave de local.properties (${trimmedKey.take(8)}...) no es una API Key de Google Gemini. Las claves válidas de Google AI Studio empiezan por 'AIzaSy'. Obtén una gratis en aistudio.google.com")
        }

        val prompt = "Eres un tutor de matemáticas amigable. " +
                "El estudiante respondió a la pregunta: '$question'. " +
                "Su respuesta fue: '$studentAnswer', pero la respuesta correcta es: '$correctAnswer'. " +
                (if (reference != null) "Explicación de referencia: '$reference'. " else "") +
                "Explícale amablemente y en español dónde estuvo su error y cómo llegar al resultado correcto. " +
                "Sé directo y breve (máximo 2 o 3 oraciones). No digas que su respuesta fue correcta."

        val body = "{\"contents\":[{\"parts\":[{\"text\":" + MiniJson.quote(prompt) + "}]}]}"

        var lastException: IOException? = null

        for ((modelName, apiVersion) in candidateModels) {
            val urlString = "https://generativelanguage.googleapis.com/$apiVersion/models/$modelName:generateContent?key=$trimmedKey"
            val conn = URL(urlString).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = connectTimeoutMs
                conn.readTimeout = readTimeoutMs
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val status = conn.responseCode
                if (status == 200) {
                    val text = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                    return MiniJson.readString(text, "text")
                        ?: throw IOException("Respuesta de Gemini sin el campo 'text'")
                } else {
                    val errText = try {
                        conn.errorStream?.use { it.readBytes().toString(Charsets.UTF_8) }
                    } catch (_: Exception) {
                        null
                    }
                    lastException = IOException("Gemini ($modelName) respondió con código $status: ${errText ?: ""}")
                    if (status == 404 || status == 503 || status == 429 || status == 500) {
                        // Modelo no encontrado o con alta demanda temporal en Google, probar el siguiente modelo
                        continue
                    } else {
                        // Error irrecuperable (ej: 400 o 403 clave sin permisos), lanzar la excepción
                        throw lastException
                    }
                }
            } finally {
                conn.disconnect()
            }
        }

        throw lastException ?: IOException("No se pudo conectar a ningún modelo de Gemini")
    }
}
