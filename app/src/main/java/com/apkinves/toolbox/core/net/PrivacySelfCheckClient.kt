package com.apkinves.toolbox.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrivacySelfCheckClient {

    data class PrivacyReport(
        val publicIp: String?,
        val ispOrg: String?,
        val country: String?,
        val dnsServers: List<String>,
        val usingKnownPublicDns: Boolean,
    )

    private val KNOWN_PUBLIC_DNS = setOf(
        "8.8.8.8", "8.8.4.4", // Google
        "1.1.1.1", "1.0.0.1", // Cloudflare
        "9.9.9.9", // Quad9
        "208.67.222.222", "208.67.220.220", // OpenDNS
    )

    suspend fun check(context: Context): PrivacyReport = withContext(Dispatchers.IO) {
        val dnsServers = getDnsServers(context)
        val info = runCatching { IpInfoClient.lookup("") }.getOrNull() // IP vacía = ip-api detecta la IP de origen de la petición

        PrivacyReport(
            publicIp = info?.query?.ifBlank { null },
            ispOrg = info?.isp?.ifBlank { info.org },
            country = info?.country?.ifBlank { null },
            dnsServers = dnsServers,
            usingKnownPublicDns = dnsServers.any { it in KNOWN_PUBLIC_DNS },
        )
    }

    private fun getDnsServers(context: Context): List<String> {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network: Network = cm.activeNetwork ?: return emptyList()
            val props: LinkProperties = cm.getLinkProperties(network) ?: return emptyList()
            props.dnsServers.map { it.hostAddress ?: it.toString() }
        }.getOrDefault(emptyList())
    }
}
