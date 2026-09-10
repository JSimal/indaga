package com.apkinves.toolbox.core.util

/** Decodificador básico de VIN (número de bastidor, ISO 3779): fabricante/país por WMI, año por el carácter 10, y validación del dígito de control (norma NHTSA/Norteamérica). */
object VinDecoder {
    data class VinInfo(
        val vin: String,
        val validLength: Boolean,
        val checkDigitValid: Boolean?,
        val country: String?,
        val manufacturer: String?,
        val possibleYears: List<Int>,
    )

    // Tabla WMI (prefijo de 2-3 caracteres) reducida a fabricantes/países habituales.
    private val WMI_TABLE = mapOf(
        "1G1" to ("Estados Unidos" to "Chevrolet"), "1FA" to ("Estados Unidos" to "Ford"),
        "1FT" to ("Estados Unidos" to "Ford (camioneta)"), "1HG" to ("Estados Unidos" to "Honda"),
        "1N4" to ("Estados Unidos" to "Nissan"), "1C4" to ("Estados Unidos" to "Chrysler/Jeep"),
        "2T1" to ("Canadá" to "Toyota"), "3VW" to ("México" to "Volkswagen"),
        "4T1" to ("Estados Unidos" to "Toyota"), "5YJ" to ("Estados Unidos" to "Tesla"),
        "JHM" to ("Japón" to "Honda"), "JN1" to ("Japón" to "Nissan"), "JT2" to ("Japón" to "Toyota"),
        "JTD" to ("Japón" to "Toyota"), "KMH" to ("Corea del Sur" to "Hyundai"),
        "KNA" to ("Corea del Sur" to "Kia"), "SAJ" to ("Reino Unido" to "Jaguar"),
        "SAL" to ("Reino Unido" to "Land Rover"), "VF1" to ("Francia" to "Renault"),
        "VF3" to ("Francia" to "Peugeot"), "VF7" to ("Francia" to "Citroën"),
        "VSS" to ("España" to "SEAT"), "WAU" to ("Alemania" to "Audi"),
        "WBA" to ("Alemania" to "BMW"), "WDB" to ("Alemania" to "Mercedes-Benz"),
        "WDD" to ("Alemania" to "Mercedes-Benz"), "WP0" to ("Alemania" to "Porsche"),
        "WVW" to ("Alemania" to "Volkswagen"), "WV1" to ("Alemania" to "Volkswagen (comercial)"),
        "ZAR" to ("Italia" to "Alfa Romeo"), "ZFA" to ("Italia" to "Fiat"),
        "ZFF" to ("Italia" to "Ferrari"), "ZHW" to ("Italia" to "Lamborghini"),
        "YV1" to ("Suecia" to "Volvo"), "YS3" to ("Suecia" to "Saab"),
        "TMB" to ("República Checa" to "Škoda"), "TRU" to ("Eslovaquia" to "Audi (planta Győr/otras)"),
    )

    // Carácter 10 del VIN: código de año-modelo (ciclo de 30 años; se ofrecen las dos posibilidades habituales).
    private val YEAR_CODES = "ABCDEFGHJKLMNPRSTVWXY123456789"
    private fun yearsForCode(c: Char): List<Int> {
        val idx = YEAR_CODES.indexOf(c.uppercaseChar())
        if (idx < 0) return emptyList()
        // El ciclo se repite cada 30 posiciones; 1980 y 2010 comparten "A", etc.
        return listOf(1980 + idx, 2010 + idx)
    }

    private val TRANSLITERATION = mapOf(
        'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6, 'G' to 7, 'H' to 8,
        'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4, 'N' to 5, 'P' to 7, 'R' to 9,
        'S' to 2, 'T' to 3, 'U' to 4, 'V' to 5, 'W' to 6, 'X' to 7, 'Y' to 8, 'Z' to 9,
    )
    private val WEIGHTS = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)

    private fun checkDigit(vin: String): Char? {
        if (vin.length != 17) return null
        var sum = 0
        for (i in 0 until 17) {
            val c = vin[i]
            val value = when {
                c.isDigit() -> c - '0'
                TRANSLITERATION.containsKey(c) -> TRANSLITERATION.getValue(c)
                else -> return null
            }
            sum += value * WEIGHTS[i]
        }
        val remainder = sum % 11
        return if (remainder == 10) 'X' else ('0' + remainder)
    }

    fun decode(rawVin: String): VinInfo {
        val vin = rawVin.trim().uppercase()
        val validLength = vin.length == 17 && vin.none { it == 'I' || it == 'O' || it == 'Q' }

        val wmiEntry = (3 downTo 2).asSequence()
            .mapNotNull { len -> vin.take(len).let { prefix -> WMI_TABLE[prefix]?.let { prefix to it } } }
            .firstOrNull()

        val checkDigitValid = if (validLength) checkDigit(vin)?.let { it == vin[8] } else null
        val possibleYears = if (vin.length >= 10) yearsForCode(vin[9]) else emptyList()

        return VinInfo(
            vin = vin,
            validLength = validLength,
            checkDigitValid = checkDigitValid,
            country = wmiEntry?.second?.first,
            manufacturer = wmiEntry?.second?.second,
            possibleYears = possibleYears,
        )
    }
}
