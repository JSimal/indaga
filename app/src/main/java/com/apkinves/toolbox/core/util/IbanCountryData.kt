package com.apkinves.toolbox.core.util

object IbanCountryData {
    data class IbanCountry(val code: String, val country: String, val length: Int)

    val COUNTRIES = listOf(
        IbanCountry("ES", "España", 24),
        IbanCountry("PT", "Portugal", 25),
        IbanCountry("FR", "Francia", 27),
        IbanCountry("DE", "Alemania", 22),
        IbanCountry("IT", "Italia", 27),
        IbanCountry("GB", "Reino Unido", 22),
        IbanCountry("IE", "Irlanda", 22),
        IbanCountry("NL", "Países Bajos", 18),
        IbanCountry("BE", "Bélgica", 16),
        IbanCountry("CH", "Suiza", 21),
        IbanCountry("AT", "Austria", 20),
        IbanCountry("LU", "Luxemburgo", 20),
        IbanCountry("PL", "Polonia", 28),
        IbanCountry("SE", "Suecia", 24),
        IbanCountry("NO", "Noruega", 15),
        IbanCountry("DK", "Dinamarca", 18),
        IbanCountry("FI", "Finlandia", 18),
        IbanCountry("GR", "Grecia", 27),
        IbanCountry("CZ", "República Checa", 24),
        IbanCountry("SK", "Eslovaquia", 24),
        IbanCountry("HU", "Hungría", 28),
        IbanCountry("RO", "Rumanía", 24),
        IbanCountry("BG", "Bulgaria", 22),
        IbanCountry("HR", "Croacia", 21),
        IbanCountry("SI", "Eslovenia", 19),
        IbanCountry("EE", "Estonia", 20),
        IbanCountry("LV", "Letonia", 21),
        IbanCountry("LT", "Lituania", 20),
        IbanCountry("MT", "Malta", 31),
        IbanCountry("CY", "Chipre", 28),
        IbanCountry("AD", "Andorra", 24),
        IbanCountry("SM", "San Marino", 27),
        IbanCountry("TR", "Turquía", 26),
        IbanCountry("SA", "Arabia Saudí", 24),
        IbanCountry("AE", "Emiratos Árabes Unidos", 23),
        IbanCountry("IL", "Israel", 23),
        IbanCountry("BR", "Brasil", 29),
        IbanCountry("MC", "Mónaco", 27),
        IbanCountry("GI", "Gibraltar", 23),
        IbanCountry("IS", "Islandia", 26),
        IbanCountry("XK", "Kosovo", 20),
    ).sortedBy { it.country }

    fun byCode(code: String): IbanCountry? = COUNTRIES.firstOrNull { it.code.equals(code, ignoreCase = true) }
}
