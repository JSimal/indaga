package com.apkinves.toolbox.core.util

/** Bancos de palabras concepto (español) por temática, e idiomas "exóticos" en alfabeto latino para traducirlas. */
object NameGenerator {
    enum class Theme(val label: String) {
        GENERAL("General"),
        PELICULAS("Películas"),
        FRIKIS("Frikis"),
        MARVEL("Marvel"),
        TRONOS("Juego de tronos"),
        MATRIX("Matrix"),
        CIBER("Ciber"),
        MITOLOGIA("Mitología"),
        ESPACIO("Espacio"),
    }

    data class Language(val code: String, val label: String)

    /** Idiomas con alfabeto latino que suelen dar resultados legibles y llamativos. Alemán primero: es el que más juego da. */
    val LANGUAGES = listOf(
        Language("de", "Alemán"),
        Language("fi", "Finés"),
        Language("is", "Islandés"),
        Language("eu", "Euskera"),
        Language("cy", "Galés"),
        Language("ga", "Irlandés"),
        Language("et", "Estonio"),
        Language("hu", "Húngaro"),
        Language("tr", "Turco"),
        Language("sw", "Suajili"),
        Language("mi", "Maorí"),
        Language("id", "Indonesio"),
        Language("vi", "Vietnamita"),
        Language("cs", "Checo"),
        Language("pl", "Polaco"),
        Language("hr", "Croata"),
        Language("no", "Noruego"),
        Language("sv", "Sueco"),
        Language("da", "Danés"),
        Language("nl", "Neerlandés"),
        Language("it", "Italiano"),
        Language("af", "Afrikáans"),
        Language("so", "Somalí"),
        Language("tl", "Filipino"),
        Language("lv", "Letón"),
        Language("lt", "Lituano"),
        Language("sq", "Albanés"),
        Language("mt", "Maltés"),
        Language("eo", "Esperanto"),
        Language("ca", "Catalán"),
        Language("gl", "Gallego"),
        Language("zu", "Zulú"),
        Language("xh", "Xhosa"),
        Language("yo", "Yoruba"),
        Language("sm", "Samoano"),
        Language("ro", "Rumano"),
        Language("fr", "Francés"),
        Language("pt", "Portugués"),
        Language("sk", "Eslovaco"),
        Language("sl", "Esloveno"),
    )

    private val WORDS: Map<Theme, List<String>> = mapOf(
        Theme.GENERAL to listOf(
            "despierto", "sombra", "fuego", "trueno", "estrella", "libertad", "silencio", "tormenta",
            "fantasma", "cazador", "guardián", "viajero", "eco", "relámpago", "aurora", "niebla",
            "espejo", "vigilante", "errante", "nómada", "centinela", "amanecer", "ocaso", "abismo",
            "horizonte", "tempestad", "susurro", "refugio", "destino", "enigma",
        ),
        Theme.PELICULAS to listOf(
            "redención", "venganza", "traición", "renacer", "fugitivo", "legado", "odisea", "vendetta",
            "renegado", "sacrificio", "resistencia", "eclipse", "némesis", "escape", "conspiración",
            "revelación", "caída", "ascenso", "impostor", "testigo", "cómplice", "rehén", "emboscada",
        ),
        Theme.FRIKIS to listOf(
            "pixel", "circuito", "nebulosa", "cuántico", "vector", "nova", "prisma", "vórtice",
            "hexágono", "algoritmo", "simulación", "holograma", "sintético", "digital", "binario",
            "androide", "cíborg", "servidor", "terminal", "portal", "anomalía", "singularidad", "paradoja",
        ),
        Theme.MARVEL to listOf(
            "multiverso", "infinito", "cuántico", "cósmico", "escudo", "trueno", "poder", "mutación",
            "guardián", "titán", "estelar", "dimensión", "anomalía", "portal", "energía", "fusión",
            "catalizador", "metamorfosis", "vibración",
        ),
        Theme.TRONOS to listOf(
            "invierno", "dragón", "trono", "cuervo", "muro", "lobo", "fuego", "hielo", "corona",
            "corriente", "espada", "escudo", "estandarte", "linaje", "reino", "traición", "conquista",
            "profecía", "guerra", "alianza", "exilio", "heredero", "usurpador", "vasallo",
        ),
        Theme.MATRIX to listOf(
            "matriz", "código", "simulación", "realidad", "despertar", "oráculo", "nexo", "binario",
            "arquitecto", "anomalía", "programa", "sistema", "ilusión", "consciencia", "elegido",
            "rebelión", "enjambre", "interfaz", "constructo", "espejismo",
        ),
        Theme.CIBER to listOf(
            "cortafuegos", "núcleo", "intermediario", "carga", "fantasma", "cifrado", "vulnerabilidad",
            "puerta trasera", "intrusión", "anonimato", "encriptación", "rastro", "huella", "camuflaje",
            "infiltración", "vigilancia", "contraseña", "protocolo", "nodo", "enjambre",
        ),
        Theme.MITOLOGIA to listOf(
            "olimpo", "titán", "oráculo", "hidra", "cíclope", "minotauro", "quimera", "destino",
            "profecía", "inmortal", "divino", "sagrado", "maldición", "ofrenda", "templo", "guardián",
            "leyenda", "origen",
        ),
        Theme.ESPACIO to listOf(
            "nebulosa", "cometa", "supernova", "meteoro", "galaxia", "eclipse", "órbita", "estelar",
            "cuásar", "constelación", "horizonte", "pulsar", "asteroide", "satélite", "cosmos", "vacío",
            "infinito", "gravedad", "nova",
        ),
    )

    /** Todas las palabras de todas las temáticas, para el modo "cualquiera". */
    private val ALL_WORDS: List<String> = WORDS.values.flatten().distinct()

    fun randomWord(theme: Theme?): String =
        if (theme == null) ALL_WORDS.random() else WORDS.getValue(theme).random()

    fun randomLanguage(): Language = LANGUAGES.random()
}
