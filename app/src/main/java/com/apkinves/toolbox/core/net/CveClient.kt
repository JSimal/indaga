package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
private data class NvdResponse(val vulnerabilities: List<NvdVulnWrapper> = emptyList())

@Serializable
private data class NvdVulnWrapper(val cve: NvdCve)

@Serializable
private data class NvdCve(
    val id: String = "",
    val published: String = "",
    val descriptions: List<NvdDescription> = emptyList(),
    val metrics: NvdMetrics = NvdMetrics(),
    val references: List<NvdReference> = emptyList(),
)

@Serializable
private data class NvdDescription(val lang: String = "", val value: String = "")

@Serializable
private data class NvdMetrics(
    val cvssMetricV31: List<NvdCvssMetric> = emptyList(),
    val cvssMetricV30: List<NvdCvssMetric> = emptyList(),
    val cvssMetricV2: List<NvdCvssMetric> = emptyList(),
)

@Serializable
private data class NvdCvssMetric(val cvssData: NvdCvssData = NvdCvssData(), val baseSeverity: String? = null)

@Serializable
private data class NvdCvssData(val baseScore: Double = 0.0, val baseSeverity: String? = null)

@Serializable
private data class NvdReference(val url: String = "")

/**
 * Consulta la NVD (National Vulnerability Database) del NIST, la base de
 * datos oficial de CVEs, gratis y sin necesidad de key para uso puntual
 * (límite de cortesía: 5 peticiones cada 30s sin registrarse, de sobra para
 * una búsqueda manual).
 */
object CveClient {

    data class CveResult(
        val id: String,
        val description: String,
        val score: Double?,
        val severity: String?,
        val published: String,
        val detailUrl: String,
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(keyword: String, maxResults: Int = 20): Result<List<CveResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode(keyword.trim(), "UTF-8")
            val url = "https://services.nvd.nist.gov/rest/json/cves/2.0?keywordSearch=$encoded&resultsPerPage=$maxResults"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            val body = try {
                if (conn.responseCode !in 200..299) error("NVD respondió con código ${conn.responseCode} (puede que haya que esperar unos segundos por límite de peticiones)")
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }

            val parsed = json.decodeFromString(NvdResponse.serializer(), body)
            parsed.vulnerabilities.map { wrapper ->
                val cve = wrapper.cve
                val metric = cve.metrics.cvssMetricV31.firstOrNull()
                    ?: cve.metrics.cvssMetricV30.firstOrNull()
                    ?: cve.metrics.cvssMetricV2.firstOrNull()
                val score = metric?.cvssData?.baseScore
                val severity = metric?.cvssData?.baseSeverity ?: metric?.baseSeverity
                CveResult(
                    id = cve.id,
                    description = cve.descriptions.firstOrNull { it.lang == "en" }?.value ?: cve.descriptions.firstOrNull()?.value ?: "(sin descripción)",
                    score = score,
                    severity = severity,
                    published = cve.published.take(10),
                    detailUrl = "https://nvd.nist.gov/vuln/detail/${cve.id}",
                )
            }.sortedByDescending { it.score ?: 0.0 }
        }
    }
}
