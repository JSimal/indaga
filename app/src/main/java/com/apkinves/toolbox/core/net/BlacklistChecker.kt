package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Comprueba una IP contra listas negras DNSBL públicas usando el propio
 * protocolo DNS (sin API ni key): se invierte la IP y se consulta como
 * subdominio de cada zona DNSBL; si resuelve, está listada.
 */
object BlacklistChecker {

    private val DNSBL_ZONES = listOf(
        "zen.spamhaus.org",
        "bl.spamcop.net",
        "b.barracudacentral.org",
        "dnsbl.sorbs.net",
    )

    data class BlacklistResult(val zone: String, val listed: Boolean)

    suspend fun check(ip: String): List<BlacklistResult> = withContext(Dispatchers.IO) {
        val reversed = ip.trim().split(".").reversed().joinToString(".")
        DNSBL_ZONES.map { zone ->
            async {
                val listed = runCatching {
                    InetAddress.getAllByName("$reversed.$zone").isNotEmpty()
                }.getOrDefault(false)
                BlacklistResult(zone, listed)
            }
        }.awaitAll()
    }
}
