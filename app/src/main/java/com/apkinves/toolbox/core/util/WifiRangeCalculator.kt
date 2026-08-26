package com.apkinves.toolbox.core.util

import kotlin.math.log10
import kotlin.math.pow

/**
 * Estimación teórica de alcance en espacio libre (Friis), útil como orden de
 * magnitud: en interiores el alcance real es muy inferior por paredes/obstáculos.
 */
object WifiRangeCalculator {

    data class RangeResult(val theoreticalRangeMeters: Double, val pathLossAt10m: Double)

    fun estimateRange(
        txPowerDbm: Double,
        txGainDbi: Double,
        rxGainDbi: Double,
        rxSensitivityDbm: Double,
        frequencyMhz: Double,
    ): RangeResult {
        val eirp = txPowerDbm + txGainDbi
        val maxPathLoss = eirp + rxGainDbi - rxSensitivityDbm

        // Free-space path loss (dB) = 20*log10(d_km) + 20*log10(f_MHz) + 32.44
        // Despejamos d en km:
        val distanceKm = 10.0.pow((maxPathLoss - 20 * log10(frequencyMhz) - 32.44) / 20.0)
        val distanceMeters = distanceKm * 1000

        val pathLossAt10m = 20 * log10(0.01) + 20 * log10(frequencyMhz) + 32.44

        return RangeResult(distanceMeters, pathLossAt10m)
    }
}
