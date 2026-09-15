package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprueba en paralelo si los servicios externos gratuitos de los que
 * depende la app están respondiendo. Solo mira si el servicio contesta
 * algo razonable (código HTTP + latencia), no si los datos son correctos.
 * El resultado de cada servicio se cachea un minuto para no machacar las
 * APIs si el usuario abre el panel varias veces seguidas.
 */
object ServiceHealthClient {

    enum class Status { UP, DEGRADED, DOWN }

    data class ServiceCheck(
        val name: String,
        val url: String,
        val status: Status,
        val httpCode: Int?,
        val rttMs: Long?,
        val detail: String?,
    )

    private data class Probe(val name: String, val url: String, val expectedCodes: IntRange = 200..299)

    private val services = listOf(
        Probe("RDAP (rdap.org)", "https://rdap.org/ip/1.1.1.1"),
        Probe("Wayback Machine", "https://archive.org/wayback/available?url=example.com"),
        Probe("Have I Been Pwned", "https://api.pwnedpasswords.com/range/00000"),
        Probe("Google DNS-over-HTTPS", "https://dns.google/resolve?name=example.com"),
        Probe("NVD (CVE)", "https://services.nvd.nist.gov/rest/json/cves/2.0?resultsPerPage=1"),
        Probe("Frankfurter (divisas)", "https://api.frankfurter.app/latest?from=EUR&to=USD"),
        Probe("CoinGecko", "https://api.coingecko.com/api/v3/ping"),
        Probe("Yahoo Finance", "https://query1.finance.yahoo.com/v8/finance/chart/BTC-USD"),
        Probe("MyMemory (traductor)", "https://api.mymemory.translated.net/get?q=hola&langpair=es|en"),
        Probe("Binlist (BIN de tarjetas)", "https://lookup.binlist.net/45717360"),
        Probe("Nominatim (OpenStreetMap)", "https://nominatim.openstreetmap.org/reverse?lat=40.4&lon=-3.7&format=json"),
        Probe("Overpass API (cajeros)", "https://overpass-api.de/api/interpreter?data=[out:json];out;"),
        Probe("crt.sh (subdominios)", "https://crt.sh/?q=%25.example.com&output=json"),
        Probe("CertSpotter (subdominios)", "https://api.certspotter.com/v1/issuances?domain=example.com"),
        Probe("ip-api.com (geolocalización IP)", "http://ip-api.com/json/1.1.1.1"),
        Probe("OpenPhish (feed)", "https://openphish.com/feed.txt"),
        Probe("Shodan InternetDB", "https://internetdb.shodan.io/1.1.1.1"),
        Probe("GitHub API (actualizaciones)", "https://api.github.com/repos/JSimal/indaga/releases/latest"),
    )

    private val cache = TtlCache<String, ServiceCheck>(60 * 1000L)

    suspend fun checkAll(): List<ServiceCheck> = coroutineScope {
        services.map { svc -> async { cache.getOrFetch(svc.name) { probe(svc) } } }.awaitAll()
    }

    private suspend fun probe(p: Probe): ServiceCheck = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        runCatching {
            val conn = NetClient.openConnection(p.url)
            conn.connectTimeout = 6000
            conn.readTimeout = 8000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "Indaga-HealthCheck/1.0")
            try {
                val code = conn.responseCode
                runCatching { conn.errorStream?.close() }
                runCatching { conn.inputStream?.close() }
                code
            } finally {
                conn.disconnect()
            }
        }.fold(
            onSuccess = { code ->
                val rtt = System.currentTimeMillis() - start
                val status = when {
                    code in p.expectedCodes -> if (rtt > 6000) Status.DEGRADED else Status.UP
                    code in 400..499 -> Status.DEGRADED
                    else -> Status.DOWN
                }
                ServiceCheck(p.name, p.url, status, code, rtt, if (status == Status.DEGRADED) "Código $code" else null)
            },
            onFailure = { e ->
                ServiceCheck(p.name, p.url, Status.DOWN, null, null, e.message ?: "Sin respuesta")
            },
        )
    }
}
