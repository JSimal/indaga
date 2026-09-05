package com.apkinves.toolbox.core.util

/** Generador de nombres aleatorios de dos palabras, con temáticas variadas. Útil para alias, proyectos, personajes o nombres en clave de cualquier tipo. */
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

    private val WORDS_A: Map<Theme, List<String>> = mapOf(
        Theme.GENERAL to listOf("Águila", "Lobo", "Halcón", "Sombra", "Trueno", "Cuervo", "Tigre", "Fénix", "Pantera", "Serpiente"),
        Theme.PELICULAS to listOf("Amanecer", "Fugitivo", "Redención", "Eclipse", "Némesis", "Odisea", "Vendetta", "Renegado", "Legado", "Escape"),
        Theme.FRIKIS to listOf("Pixel", "Nova", "Vector", "Byte", "Nebulosa", "Quantum", "Nexo", "Prisma", "Vórtice", "Circuito"),
        Theme.MARVEL to listOf("Vibranium", "Multiverso", "Infinito", "Mjölnir", "Escudo", "Cuántico", "Snap", "Ragnarok", "Asgard", "Estelar"),
        Theme.TRONOS to listOf("Invierno", "Dragón", "Trono", "Cuervo", "Muro", "Lobo", "Fuego", "Hielo", "Corona", "Corriente"),
        Theme.MATRIX to listOf("Matriz", "Código", "Pastilla", "Simulación", "Oráculo", "Nexus", "Binario", "Despertar", "Realidad", "Arquitecto"),
        Theme.CIBER to listOf("Firewall", "Kernel", "Proxy", "Payload", "Ghost", "Phantom", "Cifrado", "Exploit", "Backdoor", "Rootkit"),
        Theme.MITOLOGIA to listOf("Olimpo", "Valhalla", "Titán", "Oráculo", "Caronte", "Fénix", "Hidra", "Cíclope", "Minotauro", "Quimera"),
        Theme.ESPACIO to listOf("Nebulosa", "Cometa", "Supernova", "Meteoro", "Galaxia", "Eclipse", "Nova", "Órbita", "Estelar", "Cuásar"),
    )

    private val WORDS_B: Map<Theme, List<String>> = mapOf(
        Theme.GENERAL to listOf("Nocturno", "Escarlata", "Silencioso", "Errante", "Veloz", "Oculto", "Dorado", "Salvaje", "Ártico", "Furtivo"),
        Theme.PELICULAS to listOf("Final", "Oscuro", "Eterno", "Perdido", "Absoluto", "Infinito", "Secreto", "Mortal", "Imparable", "Definitivo"),
        Theme.FRIKIS to listOf("Digital", "Cuántico", "Binario", "Sintético", "Holográfico", "Neural", "Virtual", "Criogénico", "Modular", "Recursivo"),
        Theme.MARVEL to listOf("Cósmico", "Eterno", "Absoluto", "Ancestral", "Dimensional", "Primigenio", "Estelar", "Inquebrantable", "Colosal", "Trascendente"),
        Theme.TRONOS to listOf("del Norte", "de Hierro", "Eterno", "de Sangre", "Silencioso", "del Sur", "Ancestral", "de Fuego", "Nocturno", "Perdido"),
        Theme.MATRIX to listOf("Rojo", "Azul", "Digital", "Simulado", "Despierto", "Anómalo", "Recursivo", "Cifrado", "Latente", "Residual"),
        Theme.CIBER to listOf("Nulo", "Oculto", "Cifrado", "Remoto", "Silencioso", "Anónimo", "Persistente", "Fantasma", "Encriptado", "Invisible"),
        Theme.MITOLOGIA to listOf("Eterno", "Sagrado", "Ancestral", "Divino", "Maldito", "Inmortal", "Primigenio", "Legendario", "Perdido", "Profético"),
        Theme.ESPACIO to listOf("Distante", "Binario", "Errante", "Silencioso", "Profundo", "Helado", "Ardiente", "Lejano", "Oscuro", "Infinito"),
    )

    fun generate(theme: Theme, count: Int = 5): List<String> {
        val a = WORDS_A.getValue(theme)
        val b = WORDS_B.getValue(theme)
        val results = linkedSetOf<String>()
        var attempts = 0
        while (results.size < count && attempts < count * 20) {
            results += "${a.random()} ${b.random()}"
            attempts++
        }
        return results.toList()
    }
}
