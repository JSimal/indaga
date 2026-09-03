package com.apkinves.toolbox.core.util

import kotlin.math.floor

/** Open Location Code (Plus Codes) de Google: algoritmo abierto, sin API ni clave. Códigos completos de 10 dígitos (~14m de precisión). */
object PlusCodes {
    private const val SEPARATOR = '+'
    private const val SEPARATOR_POSITION = 8
    private const val CODE_ALPHABET = "23456789CFGHJMPQRVWX"
    private const val LATITUDE_MAX = 90.0
    private const val LONGITUDE_MAX = 180.0
    private const val PAIR_CODE_LENGTH = 10
    private val PAIR_RESOLUTIONS = doubleArrayOf(20.0, 1.0, 0.05, 0.0025, 0.000125)

    fun encode(latitude: Double, longitude: Double): String {
        var lat = latitude.coerceIn(-LATITUDE_MAX, LATITUDE_MAX)
        if (lat == LATITUDE_MAX) lat -= PAIR_RESOLUTIONS.last()
        var lon = longitude
        while (lon < -LONGITUDE_MAX) lon += 360.0
        while (lon >= LONGITUDE_MAX) lon -= 360.0

        var adjustedLat = lat + LATITUDE_MAX
        var adjustedLon = lon + LONGITUDE_MAX

        val code = StringBuilder()
        var digitCount = 0
        while (digitCount < PAIR_CODE_LENGTH) {
            val placeValue = PAIR_RESOLUTIONS[digitCount / 2]

            var digitValue = floor(adjustedLat / placeValue).toInt()
            adjustedLat -= digitValue * placeValue
            code.append(CODE_ALPHABET[digitValue])
            digitCount++

            digitValue = floor(adjustedLon / placeValue).toInt()
            adjustedLon -= digitValue * placeValue
            code.append(CODE_ALPHABET[digitValue])
            digitCount++

            if (digitCount == SEPARATOR_POSITION && digitCount < PAIR_CODE_LENGTH) {
                code.append(SEPARATOR)
            }
        }
        if (code.length < SEPARATOR_POSITION) {
            code.append("0".repeat(SEPARATOR_POSITION - code.length))
        }
        if (code.length == SEPARATOR_POSITION) {
            code.append(SEPARATOR)
        }
        return code.toString()
    }

    data class DecodedArea(val centerLat: Double, val centerLon: Double, val loLat: Double, val loLon: Double, val hiLat: Double, val hiLon: Double)

    /** Decodifica un código completo (con '+'). Devuelve null si el formato no es válido. */
    fun decode(code: String): DecodedArea? {
        val clean = code.trim().uppercase().replace(SEPARATOR.toString(), "")
        if (clean.isEmpty() || clean.length > PAIR_CODE_LENGTH) return null
        if (!clean.all { CODE_ALPHABET.contains(it) || it == '0' }) return null

        var southLat = -LATITUDE_MAX
        var westLon = -LONGITUDE_MAX
        var latResolution = 400.0
        var lonResolution = 400.0

        var i = 0
        while (i < clean.length && i < PAIR_CODE_LENGTH) {
            val placeValue = PAIR_RESOLUTIONS[i / 2]
            if (i % 2 == 0) {
                latResolution = placeValue
                val digit = CODE_ALPHABET.indexOf(clean[i])
                if (digit < 0) return null
                southLat += digit * placeValue
            } else {
                lonResolution = placeValue
                val digit = CODE_ALPHABET.indexOf(clean[i])
                if (digit < 0) return null
                westLon += digit * placeValue
            }
            i++
        }

        val hiLat = southLat + latResolution
        val hiLon = westLon + lonResolution
        return DecodedArea(
            centerLat = (southLat + hiLat) / 2,
            centerLon = (westLon + hiLon) / 2,
            loLat = southLat,
            loLon = westLon,
            hiLat = hiLat,
            hiLon = hiLon,
        )
    }
}
