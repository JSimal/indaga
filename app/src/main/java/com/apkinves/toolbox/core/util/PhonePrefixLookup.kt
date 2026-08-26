package com.apkinves.toolbox.core.util

object PhonePrefixLookup {

    // Prefijos internacionales más comunes. Tabla estática, sin dependencias.
    private val PREFIXES = mapOf(
        "1" to "Estados Unidos / Canadá",
        "7" to "Rusia / Kazajistán",
        "20" to "Egipto",
        "27" to "Sudáfrica",
        "30" to "Grecia",
        "31" to "Países Bajos",
        "32" to "Bélgica",
        "33" to "Francia",
        "34" to "España",
        "36" to "Hungría",
        "39" to "Italia",
        "40" to "Rumanía",
        "41" to "Suiza",
        "43" to "Austria",
        "44" to "Reino Unido",
        "45" to "Dinamarca",
        "46" to "Suecia",
        "47" to "Noruega",
        "48" to "Polonia",
        "49" to "Alemania",
        "51" to "Perú",
        "52" to "México",
        "53" to "Cuba",
        "54" to "Argentina",
        "55" to "Brasil",
        "56" to "Chile",
        "57" to "Colombia",
        "58" to "Venezuela",
        "60" to "Malasia",
        "61" to "Australia",
        "62" to "Indonesia",
        "63" to "Filipinas",
        "64" to "Nueva Zelanda",
        "65" to "Singapur",
        "66" to "Tailandia",
        "81" to "Japón",
        "82" to "Corea del Sur",
        "84" to "Vietnam",
        "86" to "China",
        "90" to "Turquía",
        "91" to "India",
        "92" to "Pakistán",
        "93" to "Afganistán",
        "94" to "Sri Lanka",
        "95" to "Myanmar",
        "98" to "Irán",
        "212" to "Marruecos",
        "213" to "Argelia",
        "216" to "Túnez",
        "218" to "Libia",
        "234" to "Nigeria",
        "254" to "Kenia",
        "351" to "Portugal",
        "352" to "Luxemburgo",
        "353" to "Irlanda",
        "354" to "Islandia",
        "355" to "Albania",
        "356" to "Malta",
        "357" to "Chipre",
        "358" to "Finlandia",
        "359" to "Bulgaria",
        "370" to "Lituania",
        "371" to "Letonia",
        "372" to "Estonia",
        "373" to "Moldavia",
        "374" to "Armenia",
        "375" to "Bielorrusia",
        "376" to "Andorra",
        "377" to "Mónaco",
        "378" to "San Marino",
        "380" to "Ucrania",
        "381" to "Serbia",
        "385" to "Croacia",
        "386" to "Eslovenia",
        "420" to "República Checa",
        "421" to "Eslovaquia",
        "852" to "Hong Kong",
        "886" to "Taiwán",
        "971" to "Emiratos Árabes Unidos",
        "972" to "Israel",
        "973" to "Baréin",
        "974" to "Catar",
        "966" to "Arabia Saudí",
    )

    data class PrefixResult(val prefix: String, val country: String)

    fun lookup(input: String): PrefixResult? {
        val digits = input.trim().removePrefix("+").filter { it.isDigit() }
        // probar de más largo a más corto (algunos prefijos comparten inicio, ej. 1 vs 216)
        for (len in minOf(3, digits.length) downTo 1) {
            val candidate = digits.take(len)
            PREFIXES[candidate]?.let { return PrefixResult(candidate, it) }
        }
        return null
    }
}
