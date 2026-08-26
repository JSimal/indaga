package com.apkinves.toolbox.core.util

object HttpStatusCodes {
    val CODES = mapOf(
        100 to "Continue — el servidor recibió las cabeceras, el cliente puede seguir enviando el cuerpo",
        101 to "Switching Protocols",
        200 to "OK — petición correcta",
        201 to "Created — recurso creado",
        202 to "Accepted — aceptado pero aún no procesado",
        204 to "No Content — correcto pero sin cuerpo de respuesta",
        301 to "Moved Permanently — redirección permanente",
        302 to "Found — redirección temporal",
        304 to "Not Modified — usa la copia en caché",
        307 to "Temporary Redirect — igual que 302 pero preserva el método",
        308 to "Permanent Redirect — igual que 301 pero preserva el método",
        400 to "Bad Request — la petición está mal formada",
        401 to "Unauthorized — falta autenticación",
        402 to "Payment Required",
        403 to "Forbidden — autenticado pero sin permiso",
        404 to "Not Found — recurso no encontrado",
        405 to "Method Not Allowed",
        408 to "Request Timeout",
        409 to "Conflict",
        410 to "Gone — existió pero se eliminó permanentemente",
        413 to "Payload Too Large",
        414 to "URI Too Long",
        418 to "I'm a teapot (broma del RFC 2324)",
        429 to "Too Many Requests — límite de tasa superado",
        451 to "Unavailable For Legal Reasons",
        500 to "Internal Server Error — error genérico del servidor",
        501 to "Not Implemented",
        502 to "Bad Gateway — respuesta inválida de un servidor upstream",
        503 to "Service Unavailable — servidor sobrecargado o en mantenimiento",
        504 to "Gateway Timeout — el upstream no respondió a tiempo",
        505 to "HTTP Version Not Supported",
    )

    fun lookup(code: Int): String? = CODES[code]

    fun search(query: String): List<Pair<Int, String>> {
        val q = query.trim()
        val asCode = q.toIntOrNull()
        return if (asCode != null) {
            CODES[asCode]?.let { listOf(asCode to it) } ?: emptyList()
        } else {
            CODES.filter { it.value.contains(q, ignoreCase = true) }.toList()
        }
    }
}
