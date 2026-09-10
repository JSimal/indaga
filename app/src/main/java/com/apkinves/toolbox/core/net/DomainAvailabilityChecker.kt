package com.apkinves.toolbox.core.net

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Comprueba, vía RDAP, si un mismo nombre está libre en varios TLD habituales. */
object DomainAvailabilityChecker {
    enum class Status { REGISTRADO, PROBABLEMENTE_LIBRE, DESCONOCIDO }

    data class TldResult(val tld: String, val status: Status)

    private val COMMON_TLDS = listOf("com", "net", "org", "es", "io", "dev", "app", "co")

    suspend fun checkAll(baseName: String): List<TldResult> = coroutineScope {
        val name = baseName.trim().substringBefore('.').lowercase()
        COMMON_TLDS.map { tld ->
            async {
                val domain = "$name.$tld"
                val result = RdapClient.lookupDomainStructured(domain)
                val status = when {
                    result.isSuccess -> Status.REGISTRADO
                    result.exceptionOrNull()?.message?.contains("404") == true -> Status.PROBABLEMENTE_LIBRE
                    else -> Status.DESCONOCIDO
                }
                TldResult(tld, status)
            }
        }.awaitAll()
    }
}
