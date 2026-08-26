package com.apkinves.toolbox.core.net

import com.apkinves.toolbox.core.util.CoordinateConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
private data class OverpassTags(
    val name: String? = null,
    val operator: String? = null,
    val brand: String? = null,
)

@Serializable
private data class OverpassElement(
    val lat: Double? = null,
    val lon: Double? = null,
    val tags: OverpassTags = OverpassTags(),
)

@Serializable
private data class OverpassResponse(val elements: List<OverpassElement> = emptyList())

/**
 * Cajeros automáticos vía OpenStreetMap (Overpass API): datos de comunidad,
 * gratis y sin registro. La cobertura depende de lo mapeada que esté la zona.
 */
object AtmFinderClient {

    data class Atm(val name: String, val distanceKm: Double, val lat: Double, val lon: Double)

    suspend fun findNearby(lat: Double, lon: Double, radiusMeters: Int = 1500): Result<List<Atm>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = "[out:json][timeout:15];node[\"amenity\"=\"atm\"](around:$radiusMeters,$lat,$lon);out body;"
                val encoded = URLEncoder.encode(query, "UTF-8")
                val conn = URL("https://overpass-api.de/api/interpreter?data=$encoded").openConnection() as HttpURLConnection
                conn.connectTimeout = 12000
                conn.readTimeout = 12000
                val body = try {
                    conn.inputStream.bufferedReader().readText()
                } finally {
                    conn.disconnect()
                }

                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString(OverpassResponse.serializer(), body)
                parsed.elements
                    .filter { it.lat != null && it.lon != null }
                    .map {
                        val name = it.tags.operator ?: it.tags.brand ?: it.tags.name ?: "Cajero sin identificar"
                        Atm(name, CoordinateConverter.distanceKm(lat, lon, it.lat!!, it.lon!!), it.lat, it.lon)
                    }
                    .sortedBy { it.distanceKm }
            }
        }
}
