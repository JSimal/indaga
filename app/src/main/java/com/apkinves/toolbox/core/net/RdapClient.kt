package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

data class RdapSummary(
    val name: String?,
    val handle: String?,
    val registrar: String?,
    val country: String?,
    val statuses: List<String>,
    val registeredDate: String?,
    val expiresDate: String?,
    val lastChangedDate: String?,
    val nameservers: List<String>,
    val rawText: String,
)

/**
 * RDAP (RFC 7482): el sucesor moderno de WHOIS, pero sobre HTTPS en vez de
 * el puerto 43 en crudo. Se usa como respaldo cuando el puerto 43 está
 * bloqueado (muy habitual en redes móviles/corporativas), ya que HTTPS casi
 * nunca lo está. rdap.org es un redirector público sin necesidad de key.
 */
object RdapClient {

    suspend fun lookupDomain(domain: String): Result<String> =
        lookupDomainStructured(domain).map { it.rawText }

    suspend fun lookupIp(ip: String): Result<String> =
        lookupIpStructured(ip).map { it.rawText }

    suspend fun lookupDomainStructured(domain: String): Result<RdapSummary> = withContext(Dispatchers.IO) {
        fetch("https://rdap.org/domain/${domain.trim()}")
    }

    // rdap.org redirige de forma fiable para dominios, pero para IPs falla con
    // más frecuencia (según el registro regional que gestione ese rango). Si
    // falla, se prueba directamente contra cada RIR: solo uno de ellos tendrá
    // esa IP (los demás devuelven 404, que simplemente se descarta y se sigue).
    private val IP_RDAP_SERVERS = listOf(
        "https://rdap.org/ip",
        "https://rdap.arin.net/registry/ip",
        "https://rdap.db.ripe.net/ip",
        "https://rdap.apnic.net/ip",
        "https://rdap.lacnic.net/rdap/ip",
        "https://rdap.afrinic.net/rdap/ip",
    )

    suspend fun lookupIpStructured(ip: String): Result<RdapSummary> = withContext(Dispatchers.IO) {
        var lastFailure: Result<RdapSummary>? = null
        for (base in IP_RDAP_SERVERS) {
            val result = fetch("$base/${ip.trim()}")
            if (result.isSuccess) return@withContext result
            lastFailure = result
        }
        lastFailure ?: Result.failure(IllegalStateException("Sin servidores RDAP disponibles"))
    }

    private fun fetch(url: String): Result<RdapSummary> = runCatching {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("Accept", "application/rdap+json")
        try {
            if (conn.responseCode !in 200..299) error("RDAP respondió con código ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().readText()
            parseRdap(body)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseRdap(body: String): RdapSummary {
        val json = Json.parseToJsonElement(body).jsonObject

        val name = json["ldhName"]?.jsonPrimitive?.content ?: json["name"]?.jsonPrimitive?.content
        val handle = json["handle"]?.jsonPrimitive?.content
        val country = json["country"]?.jsonPrimitive?.content

        val statuses = (json["status"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()

        var registered: String? = null
        var expires: String? = null
        var lastChanged: String? = null
        (json["events"] as? JsonArray)?.forEach { event ->
            val obj = event.jsonObject
            val action = obj["eventAction"]?.jsonPrimitive?.content
            val date = obj["eventDate"]?.jsonPrimitive?.content
            when (action) {
                "registration" -> registered = date
                "expiration" -> expires = date
                "last changed", "last update of RDAP database" -> lastChanged = lastChanged ?: date
            }
        }

        val nameservers = (json["nameservers"] as? JsonArray)?.mapNotNull {
            it.jsonObject["ldhName"]?.jsonPrimitive?.content
        } ?: emptyList()

        var registrar: String? = null
        val entitiesText = StringBuilder()
        (json["entities"] as? JsonArray)?.forEach { entity ->
            val obj = entity.jsonObject
            val roles = (obj["roles"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            val fn = extractVcardFn(obj)
            val entityHandle = obj["handle"]?.jsonPrimitive?.content
            if (roles.isNotEmpty()) entitiesText.appendLine("Entidad (${roles.joinToString(", ")}): ${fn ?: entityHandle ?: ""}")
            if ("registrar" in roles && registrar == null) registrar = fn ?: entityHandle
        }

        val rawText = buildString {
            name?.let { appendLine("Nombre: $it") }
            handle?.let { appendLine("Handle: $it") }
            country?.let { appendLine("País: $it") }
            if (statuses.isNotEmpty()) appendLine("Estado: ${statuses.joinToString(", ")}")
            registered?.let { appendLine("registration: $it") }
            expires?.let { appendLine("expiration: $it") }
            lastChanged?.let { appendLine("last changed: $it") }
            if (nameservers.isNotEmpty()) {
                appendLine("Servidores DNS:")
                nameservers.forEach { appendLine("  $it") }
            }
            append(entitiesText)
        }.trim().ifBlank { body.take(2000) }

        return RdapSummary(
            name = name,
            handle = handle,
            registrar = registrar,
            country = country,
            statuses = statuses,
            registeredDate = registered,
            expiresDate = expires,
            lastChangedDate = lastChanged,
            nameservers = nameservers,
            rawText = rawText,
        )
    }

    private fun extractVcardFn(entity: JsonObject): String? {
        val vcardArray = entity["vcardArray"] as? JsonArray ?: return null
        if (vcardArray.size < 2) return null
        val properties = vcardArray[1] as? JsonArray ?: return null
        for (prop in properties) {
            val propArray = prop as? JsonArray ?: continue
            if (propArray.size >= 4 && propArray[0].jsonPrimitive.content == "fn") {
                return propArray[3].jsonPrimitive.content
            }
        }
        return null
    }
}
