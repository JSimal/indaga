package com.apkinves.toolbox.core.util

object FrequencyBands {
    data class Band(val range: String, val name: String, val use: String)

    val BANDS = listOf(
        Band("3 kHz - 30 kHz", "VLF", "Comunicación submarina, navegación"),
        Band("30 kHz - 300 kHz", "LF", "Radio AM de onda larga, radiofaros"),
        Band("300 kHz - 3 MHz", "MF", "Radio AM"),
        Band("3 MHz - 30 MHz", "HF", "Radioaficionados, radio de onda corta"),
        Band("26.965 - 27.405 MHz", "CB (Banda Ciudadana)", "Radio CB de uso libre"),
        Band("30 MHz - 300 MHz", "VHF", "FM comercial, TV, aeronáutica, radioaficionados"),
        Band("108 - 137 MHz", "VHF Aeronáutico", "Comunicaciones de aviación civil"),
        Band("144 - 146 MHz", "VHF Radioaficionado", "Banda de 2 metros"),
        Band("300 MHz - 3 GHz", "UHF", "TV, walkie-talkies, GPS, telefonía móvil"),
        Band("380 - 470 MHz", "UHF PMR/TETRA", "Radio profesional, emergencias"),
        Band("433 MHz", "ISM 433", "Mandos a distancia, sensores IoT"),
        Band("446 MHz", "PMR446", "Walkie-talkies de uso libre"),
        Band("850 / 900 / 1800 / 1900 MHz", "GSM", "Telefonía móvil 2G/3G"),
        Band("2.4 GHz", "ISM 2.4", "WiFi, Bluetooth, microondas"),
        Band("5 GHz", "ISM 5", "WiFi de alta velocidad"),
        Band("1.5 GHz", "GPS L1", "Posicionamiento GPS civil"),
        Band("3 GHz - 30 GHz", "SHF", "Satélite, radar, WiFi 6E, 5G mmWave (parcial)"),
    )

    fun search(query: String): List<Band> {
        if (query.isBlank()) return BANDS
        return BANDS.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.use.contains(query, ignoreCase = true) ||
                it.range.contains(query, ignoreCase = true)
        }
    }
}
