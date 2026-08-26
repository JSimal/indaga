package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

object TyposquattingDetector {

    data class Candidate(val domain: String, val registered: Boolean)

    private const val KEYBOARD_ROWS = "qwertyuiop asdfghjkl zxcvbnm"

    fun generateVariants(domain: String): List<String> {
        val dotIndex = domain.lastIndexOf('.')
        if (dotIndex <= 0) return emptyList()
        val name = domain.substring(0, dotIndex)
        val tld = domain.substring(dotIndex)

        val variants = mutableSetOf<String>()

        // Omisión de una letra
        for (i in name.indices) variants.add(name.removeRange(i, i + 1) + tld)

        // Letra duplicada
        for (i in name.indices) variants.add(name.substring(0, i + 1) + name[i] + name.substring(i + 1) + tld)

        // Transposición de letras adyacentes
        for (i in 0 until name.length - 1) {
            val chars = name.toCharArray()
            val tmp = chars[i]; chars[i] = chars[i + 1]; chars[i + 1] = tmp
            variants.add(String(chars) + tld)
        }

        // Vecino de teclado
        for (i in name.indices) {
            val c = name[i]
            val row = KEYBOARD_ROWS.split(" ").firstOrNull { it.contains(c) } ?: continue
            val pos = row.indexOf(c)
            listOfNotNull(row.getOrNull(pos - 1), row.getOrNull(pos + 1)).forEach { neighbor ->
                variants.add(name.substring(0, i) + neighbor + name.substring(i + 1) + tld)
            }
        }

        // TLDs populares alternativos
        val popularTlds = listOf(".com", ".net", ".org", ".es", ".info", ".co")
        popularTlds.filter { it != tld }.forEach { variants.add(name + it) }

        variants.remove(domain)
        return variants.toList()
    }

    suspend fun checkRegistered(domains: List<String>): List<Candidate> = withContext(Dispatchers.IO) {
        domains.map { d ->
            async {
                val resolved = runCatching { DnsClient.query(d, DnsClient.RecordType.A) }.getOrElse { emptyList() }
                Candidate(d, resolved.isNotEmpty())
            }
        }.awaitAll()
    }
}
