package com.apkinves.toolbox.core.util

/** Analiza cabeceras de correo (raw headers) pegadas por el usuario: IPs de tránsito, from/to/subject/date, SPF/DKIM/DMARC. */
object EmailHeaderAnalyzer {
    data class Hop(val fromHost: String?, val byHost: String?, val ip: String?, val date: String?)

    data class Result(
        val from: String?,
        val to: String?,
        val subject: String?,
        val date: String?,
        val messageId: String?,
        val hops: List<Hop>,
        val ips: List<String>,
        val spf: String?,
        val dkim: String?,
        val dmarc: String?,
    )

    private val ipv4Regex = Regex("""\b(?:\d{1,3}\.){3}\d{1,3}\b""")
    private val ipv6Regex = Regex("""\b[0-9a-fA-F]{0,4}(?::[0-9a-fA-F]{0,4}){2,7}\b""")

    fun analyze(raw: String): Result {
        // Las cabeceras pueden venir "plegadas" en varias líneas (continuación con espacio/tab); las unimos.
        val unfolded = raw.replace(Regex("\r?\n[ \t]+"), " ")
        val lines = unfolded.lines()

        fun header(name: String): String? =
            lines.firstOrNull { it.startsWith("$name:", ignoreCase = true) }
                ?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }

        val received = lines.filter { it.startsWith("Received:", ignoreCase = true) }
        val hops = received.map { line ->
            val body = line.substringAfter(":").trim()
            val fromHost = Regex("""from\s+([^\s(]+)""", RegexOption.IGNORE_CASE).find(body)?.groupValues?.get(1)
            val byHost = Regex("""by\s+([^\s(]+)""", RegexOption.IGNORE_CASE).find(body)?.groupValues?.get(1)
            val ip = ipv4Regex.find(body)?.value ?: ipv6Regex.find(body)?.value
            val date = body.substringAfterLast(';', "").trim().takeIf { it.isNotBlank() }
            Hop(fromHost, byHost, ip, date)
        }

        val allIps = linkedSetOf<String>()
        received.forEach { line ->
            ipv4Regex.findAll(line).forEach { allIps += it.value }
        }

        val authResults = header("Authentication-Results")
        fun extractAuth(tag: String): String? =
            authResults?.let { Regex("""$tag=([a-zA-Z]+)""", RegexOption.IGNORE_CASE).find(it)?.groupValues?.get(1) }

        return Result(
            from = header("From"),
            to = header("To"),
            subject = header("Subject"),
            date = header("Date"),
            messageId = header("Message-ID") ?: header("Message-Id"),
            hops = hops,
            ips = allIps.toList(),
            spf = extractAuth("spf") ?: header("Received-SPF")?.substringBefore(" "),
            dkim = extractAuth("dkim"),
            dmarc = extractAuth("dmarc"),
        )
    }
}
