package com.practica.tutormatematicas.feedback

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente del servicio intermedio (backend en Kotlin). La aplicación NO conoce la clave de la API
 * del modelo: solo envía el ejercicio, la respuesta y la respuesta correcta, y recibe un texto.
 */
class ProxyExplanationClient(
    private val baseUrl: String,
    private val connectTimeoutMs: Int = 3000,
    private val readTimeoutMs: Int = 20000
) : ExplanationClient {

    override fun explain(question: String, studentAnswer: String, correctAnswer: String, reference: String?): String {
        val body = StringBuilder("{")
            .append("\"question\":").append(MiniJson.quote(question)).append(',')
            .append("\"studentAnswer\":").append(MiniJson.quote(studentAnswer)).append(',')
            .append("\"correctAnswer\":").append(MiniJson.quote(correctAnswer))
        if (reference != null) body.append(",\"reference\":").append(MiniJson.quote(reference))
        body.append('}')

        val conn = URL(baseUrl.trimEnd('/') + "/api/explain").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = connectTimeoutMs
            conn.readTimeout = readTimeoutMs
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val status = conn.responseCode
            if (status != 200) throw IOException("El servicio respondió con el código $status")
            val text = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            return MiniJson.readString(text, "explanation")
                ?: throw IOException("Respuesta sin el campo 'explanation'")
        } finally {
            conn.disconnect()
        }
    }
}
