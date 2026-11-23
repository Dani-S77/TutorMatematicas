package com.practica.tutormatematicas.feedback

/**
 * JSON mínimo, sin librerías, para los pocos campos de texto que se intercambian.
 * Maneja las secuencias de escape normales (\n, \", \\, \uXXXX), que los modelos
 * de lenguaje usan en sus respuestas.
 */
object MiniJson {

    /** Convierte un texto en un literal JSON entre comillas, escapando lo necesario. */
    fun quote(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append(String.format("\\u%04x", c.toInt())) else sb.append(c)
            }
        }
        return sb.append('"').toString()
    }

    /** Devuelve el primer valor de texto asociado a la clave, ya desescapado, o null si no existe. */
    fun readString(json: String, key: String): String? {
        val marker = "\"" + key + "\""
        var from = 0
        while (true) {
            val i = json.indexOf(marker, from)
            if (i < 0) return null
            var j = i + marker.length
            while (j < json.length && json[j].isWhitespace()) j++
            if (j < json.length && json[j] == ':') {
                j++
                while (j < json.length && json[j].isWhitespace()) j++
                if (j < json.length && json[j] == '"') return parseLiteral(json, j + 1)
            }
            from = i + marker.length
        }
    }

    private fun parseLiteral(json: String, start: Int): String? {
        val sb = StringBuilder()
        var i = start
        while (i < json.length) {
            val c = json[i]
            if (c == '"') return sb.toString()
            if (c == '\\' && i + 1 < json.length) {
                val n = json[i + 1]
                if (n == 'u') {
                    val code = if (i + 5 < json.length) json.substring(i + 2, i + 6).toIntOrNull(16) else null
                    if (code != null) {
                        sb.append(code.toChar())
                        i += 6
                    } else {
                        sb.append(n)
                        i += 2
                    }
                } else {
                    sb.append(unescape(n))
                    i += 2
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return null
    }

    private fun unescape(n: Char): Char = when (n) {
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        'b' -> '\b'
        else -> n
    }
}
