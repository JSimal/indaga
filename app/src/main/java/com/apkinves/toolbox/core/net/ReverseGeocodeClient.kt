package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

/** Reverse geocoding (coordenadas → dirección aproximada) vía Nominatim/OpenStreetMap, gratis y sin clave. */
object ReverseGeocodeClient {
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=16"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Indaga OSINT toolbox app")
            val body = try {
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            Json.parseToJsonElement(body).jsonObject["display_name"]?.jsonPrimitive?.content
                ?: error("Sin dirección disponible para esas coordenadas")
        }
    }
}
