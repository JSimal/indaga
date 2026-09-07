package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Traducción vía MyMemory (gratis, sin registro ni clave). */
object TranslatorClient {

    suspend fun translate(text: String, targetLangCode: String, sourceLangCode: String = "es"): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = URLEncoder.encode(text.trim(), "UTF-8")
                val url = "https://api.mymemory.translated.net/get?q=$query&langpair=$sourceLangCode|$targetLangCode"
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val body = try {
                    conn.inputStream.bufferedReader().readText()
                } finally {
                    conn.disconnect()
                }
                val translated = Json.parseToJsonElement(body).jsonObject["responseData"]?.jsonObject
                    ?.get("translatedText")?.jsonPrimitive?.content
                    ?: error("Sin traducción disponible")
                translated
            }
        }
}
