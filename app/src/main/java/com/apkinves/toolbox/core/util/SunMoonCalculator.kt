package com.apkinves.toolbox.core.util

import kotlin.math.*

/** Orto/ocaso solar y fase lunar mediante fórmulas astronómicas de dominio público (sin API). */
object SunMoonCalculator {
    data class SunTimes(
        val sunrise: String?,
        val sunset: String?,
        val dayLengthMinutes: Int?,
        val neverRises: Boolean,
        val neverSets: Boolean,
    )

    data class MoonPhase(val name: String, val illuminationPercent: Int, val ageDays: Double)

    private const val ZENITH_OFFICIAL = 90.833

    /** Algoritmo clásico de orto/ocaso (Almanac / Sun-RiseSet). utcOffsetHours: desfase de la zona horaria local respecto a UTC. */
    fun sunTimes(lat: Double, lon: Double, year: Int, month: Int, day: Int, utcOffsetHours: Double): SunTimes {
        val riseUtc = calcSunEventUtc(lat, lon, year, month, day, isSunrise = true)
        val setUtc = calcSunEventUtc(lat, lon, year, month, day, isSunrise = false)

        if (riseUtc == null || setUtc == null) {
            // cosH fuera de rango: sol de medianoche o noche polar
            val cosH = cosHourAngle(lat, year, month, day)
            return SunTimes(null, null, null, neverRises = cosH != null && cosH > 1, neverSets = cosH != null && cosH < -1)
        }

        val riseLocal = normalizeHours(riseUtc + utcOffsetHours)
        val setLocal = normalizeHours(setUtc + utcOffsetHours)
        var lengthMin = ((setLocal - riseLocal) * 60).roundToInt()
        if (lengthMin < 0) lengthMin += 24 * 60

        return SunTimes(formatHours(riseLocal), formatHours(setLocal), lengthMin, neverRises = false, neverSets = false)
    }

    private fun cosHourAngle(lat: Double, year: Int, month: Int, day: Int): Double? {
        val n = dayOfYear(year, month, day)
        val m = (0.9856 * n) - 3.289
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        l = normalizeDegrees(l)
        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))
        val cosH = (cos(Math.toRadians(ZENITH_OFFICIAL)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))
        return cosH
    }

    private fun calcSunEventUtc(lat: Double, lon: Double, year: Int, month: Int, day: Int, isSunrise: Boolean): Double? {
        val n = dayOfYear(year, month, day)
        val lngHour = lon / 15.0
        val t = if (isSunrise) n + ((6 - lngHour) / 24.0) else n + ((18 - lngHour) / 24.0)

        val m = (0.9856 * t) - 3.289
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        l = normalizeDegrees(l)

        var ra = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(l))))
        ra = normalizeDegrees(ra)
        val lQuadrant = floor(l / 90.0) * 90.0
        val raQuadrant = floor(ra / 90.0) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0

        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))
        val cosH = (cos(Math.toRadians(ZENITH_OFFICIAL)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))
        if (cosH > 1 || cosH < -1) return null

        var h = if (isSunrise) 360 - Math.toDegrees(acos(cosH)) else Math.toDegrees(acos(cosH))
        h /= 15.0

        val tLocal = h + ra - (0.06571 * t) - 6.622
        var utc = tLocal - lngHour
        utc = normalizeHours(utc)
        return utc
    }

    private fun dayOfYear(year: Int, month: Int, day: Int): Int {
        val n1 = floor(275.0 * month / 9.0)
        val n2 = floor((month + 9.0) / 12.0)
        val n3 = 1.0 + floor((year - 4.0 * floor(year / 4.0) + 2.0) / 3.0)
        return (n1 - (n2 * n3) + day - 30).toInt()
    }

    private fun normalizeDegrees(v: Double): Double {
        var r = v
        while (r < 0) r += 360
        while (r >= 360) r -= 360
        return r
    }

    private fun normalizeHours(v: Double): Double {
        var r = v
        while (r < 0) r += 24
        while (r >= 24) r -= 24
        return r
    }

    private fun formatHours(h: Double): String {
        val hh = floor(h).toInt()
        val mm = ((h - hh) * 60).roundToInt().let { if (it == 60) 0 else it }
        return "%02d:%02d".format(hh, mm)
    }

    /** Algoritmo simple (Conway) de fase lunar: precisión ~1 día, suficiente para orientación investigativa. */
    fun moonPhase(year: Int, month: Int, day: Int): MoonPhase {
        var y = year
        var m = month
        if (m < 3) {
            y--
            m += 12
        }
        m++
        val c = 365.25 * y
        val e = 30.6 * m
        var jd = c + e + day - 694039.09
        jd /= 29.5305882
        val b = floor(jd).toInt()
        val frac = jd - b
        val age = frac * 29.5305882
        var index = (frac * 8).roundToInt()
        if (index >= 8) index = 0

        val names = listOf(
            "Luna nueva", "Creciente iluminante", "Cuarto creciente", "Gibosa creciente",
            "Luna llena", "Gibosa menguante", "Cuarto menguante", "Creciente menguante",
        )
        val illumination = ((1 - cos(2 * Math.PI * age / 29.5305882)) / 2 * 100).roundToInt()
        return MoonPhase(names[index], illumination, age)
    }
}
