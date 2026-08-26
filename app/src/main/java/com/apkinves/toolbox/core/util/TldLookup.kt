package com.apkinves.toolbox.core.util

object TldLookup {

    // ccTLD -> (país, emoji de bandera). No exhaustivo, cubre los más comunes.
    private val TLDS = mapOf(
        "es" to ("España" to "🇪🇸"),
        "fr" to ("Francia" to "🇫🇷"),
        "de" to ("Alemania" to "🇩🇪"),
        "it" to ("Italia" to "🇮🇹"),
        "pt" to ("Portugal" to "🇵🇹"),
        "uk" to ("Reino Unido" to "🇬🇧"),
        "us" to ("Estados Unidos" to "🇺🇸"),
        "ca" to ("Canadá" to "🇨🇦"),
        "mx" to ("México" to "🇲🇽"),
        "ar" to ("Argentina" to "🇦🇷"),
        "br" to ("Brasil" to "🇧🇷"),
        "cl" to ("Chile" to "🇨🇱"),
        "co" to ("Colombia" to "🇨🇴"),
        "pe" to ("Perú" to "🇵🇪"),
        "ve" to ("Venezuela" to "🇻🇪"),
        "nl" to ("Países Bajos" to "🇳🇱"),
        "be" to ("Bélgica" to "🇧🇪"),
        "ch" to ("Suiza" to "🇨🇭"),
        "at" to ("Austria" to "🇦🇹"),
        "se" to ("Suecia" to "🇸🇪"),
        "no" to ("Noruega" to "🇳🇴"),
        "dk" to ("Dinamarca" to "🇩🇰"),
        "fi" to ("Finlandia" to "🇫🇮"),
        "pl" to ("Polonia" to "🇵🇱"),
        "ru" to ("Rusia" to "🇷🇺"),
        "cn" to ("China" to "🇨🇳"),
        "jp" to ("Japón" to "🇯🇵"),
        "kr" to ("Corea del Sur" to "🇰🇷"),
        "in" to ("India" to "🇮🇳"),
        "au" to ("Australia" to "🇦🇺"),
        "nz" to ("Nueva Zelanda" to "🇳🇿"),
        "za" to ("Sudáfrica" to "🇿🇦"),
        "ie" to ("Irlanda" to "🇮🇪"),
        "gr" to ("Grecia" to "🇬🇷"),
        "tr" to ("Turquía" to "🇹🇷"),
        "il" to ("Israel" to "🇮🇱"),
        "ae" to ("Emiratos Árabes Unidos" to "🇦🇪"),
        "sa" to ("Arabia Saudí" to "🇸🇦"),
        "io" to ("Territorio Británico del Océano Índico (dominio popular en tech)" to "🇮🇴"),
        "ai" to ("Anguila (dominio popular en tech/IA)" to "🇦🇮"),
        "co.uk" to ("Reino Unido" to "🇬🇧"),
        "com.mx" to ("México" to "🇲🇽"),
        "com.ar" to ("Argentina" to "🇦🇷"),
        "com.br" to ("Brasil" to "🇧🇷"),
    )

    data class TldResult(val tld: String, val country: String, val flag: String)

    fun lookup(domainOrTld: String): TldResult? {
        val cleaned = domainOrTld.trim().lowercase().removePrefix("http://").removePrefix("https://").trimEnd('/')
        val parts = cleaned.split(".")
        // Probar primero combinaciones de 2 niveles (ej: co.uk), luego 1.
        if (parts.size >= 2) {
            val lastTwo = parts.takeLast(2).joinToString(".")
            TLDS[lastTwo]?.let { return TldResult(lastTwo, it.first, it.second) }
        }
        val last = parts.lastOrNull() ?: return null
        return TLDS[last]?.let { TldResult(last, it.first, it.second) }
    }
}
