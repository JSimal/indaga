package com.apkinves.toolbox.core.util

import kotlin.math.abs
import kotlin.math.roundToInt

object CoordinateConverter {

    fun decimalToDms(decimal: Double, isLatitude: Boolean): String {
        val hemisphere = if (isLatitude) (if (decimal >= 0) "N" else "S") else (if (decimal >= 0) "E" else "W")
        val abs = abs(decimal)
        val degrees = abs.toInt()
        val minutesFull = (abs - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = (minutesFull - minutes) * 60
        return "%d°%d'%.2f\"%s".format(degrees, minutes, seconds, hemisphere)
    }

    fun dmsToDecimal(degrees: Double, minutes: Double, seconds: Double, negative: Boolean): Double {
        val value = degrees + minutes / 60.0 + seconds / 3600.0
        return if (negative) -value else value
    }

    /** Distancia entre dos puntos (fórmula de haversine), en kilómetros. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (earthRadiusKm * c * 100).roundToInt() / 100.0
    }
}
