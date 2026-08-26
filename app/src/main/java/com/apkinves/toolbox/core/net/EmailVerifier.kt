package com.apkinves.toolbox.core.net

/**
 * Verificación de email "segura": sintaxis + existencia de registros MX.
 *
 * A propósito NO intenta una verificación SMTP real (comando RCPT TO contra
 * el servidor de correo): el puerto 25 saliente está bloqueado en la
 * práctica totalidad de redes móviles y muchas domésticas (para frenar
 * botnets de spam, el mismo tipo de bloqueo que ya nos afectó con WHOIS/43),
 * así que fallaría casi siempre sin que sea un error de la app. Además,
 * muchos servidores de correo tratan esas sondas como comportamiento
 * abusivo. Sintaxis + MX es la comprobación fiable que se puede hacer bien.
 */
object EmailVerifier {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    )

    data class EmailCheckResult(
        val validSyntax: Boolean,
        val domain: String?,
        val hasMxRecords: Boolean,
        val mxHosts: List<String>,
    )

    suspend fun verify(email: String): EmailCheckResult {
        val trimmed = email.trim()
        val validSyntax = EMAIL_REGEX.matches(trimmed)
        val domain = trimmed.substringAfterLast('@', "").ifBlank { null }

        if (!validSyntax || domain == null) {
            return EmailCheckResult(validSyntax = false, domain = domain, hasMxRecords = false, mxHosts = emptyList())
        }

        val mxRecords = runCatching { DnsClient.query(domain, DnsClient.RecordType.MX) }.getOrElse { emptyList() }
        return EmailCheckResult(
            validSyntax = true,
            domain = domain,
            hasMxRecords = mxRecords.isNotEmpty(),
            mxHosts = mxRecords.map { it.value },
        )
    }
}
