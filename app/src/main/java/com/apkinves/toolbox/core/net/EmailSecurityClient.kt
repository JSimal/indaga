package com.apkinves.toolbox.core.net

/**
 * Evalúa la configuración anti-spoofing de email de un dominio (SPF, DMARC)
 * y da una nota orientativa. DKIM no se comprueba porque su registro vive en
 * un selector arbitrario que no se puede adivinar sin más contexto.
 */
object EmailSecurityClient {

    data class EmailSecurityReport(
        val spfRecord: String?,
        val dmarcRecord: String?,
        val grade: String,
        val notes: List<String>,
    )

    suspend fun analyze(domain: String): EmailSecurityReport {
        val txtRecords = runCatching { DnsClient.query(domain, DnsClient.RecordType.TXT) }.getOrElse { emptyList() }
        val spf = txtRecords.map { it.value }.firstOrNull { it.startsWith("v=spf1", ignoreCase = true) }

        val dmarcRecords = runCatching { DnsClient.query("_dmarc.$domain", DnsClient.RecordType.TXT) }.getOrElse { emptyList() }
        val dmarc = dmarcRecords.map { it.value }.firstOrNull { it.startsWith("v=DMARC1", ignoreCase = true) }

        val notes = mutableListOf<String>()
        var score = 0

        if (spf != null) {
            score += 40
            if (spf.contains("-all")) score += 10 else notes.add("SPF no usa '-all' (fallo estricto), es más permisivo de lo ideal")
        } else {
            notes.add("No se encontró registro SPF")
        }

        if (dmarc != null) {
            score += 40
            when {
                dmarc.contains("p=reject", ignoreCase = true) -> score += 10
                dmarc.contains("p=quarantine", ignoreCase = true) -> score += 5
                dmarc.contains("p=none", ignoreCase = true) -> notes.add("DMARC en modo 'none': solo monitoriza, no bloquea suplantación")
            }
        } else {
            notes.add("No se encontró registro DMARC")
        }

        val grade = when {
            score >= 90 -> "A"
            score >= 70 -> "B"
            score >= 50 -> "C"
            score >= 30 -> "D"
            else -> "F"
        }

        return EmailSecurityReport(spf, dmarc, grade, notes)
    }
}
