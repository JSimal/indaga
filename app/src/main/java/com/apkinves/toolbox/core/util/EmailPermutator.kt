package com.apkinves.toolbox.core.util

/** Genera direcciones de correo probables a partir de nombre, apellido(s) y dominio. */
object EmailPermutator {
    fun generate(nombre: String, apellido: String, dominio: String): List<String> {
        val n = nombre.trim().lowercase().normalize()
        val a = apellido.trim().lowercase().normalize()
        val d = dominio.trim().lowercase().removePrefix("@")
        if (n.isBlank() || d.isBlank()) return emptyList()

        val n1 = n.firstOrNull()?.toString() ?: ""
        val a1 = a.firstOrNull()?.toString() ?: ""

        val locals = linkedSetOf<String>()
        locals += n
        if (a.isNotBlank()) {
            locals += "$n.$a"
            locals += "$n$a"
            locals += "${n}_$a"
            locals += "$n-$a"
            locals += "$a.$n"
            locals += "$a$n"
            locals += "$n1.$a"
            locals += "$n1$a"
            locals += "$n.$a1"
            locals += "$n$a1"
            locals += "$n1$a1"
            locals += a
        }

        return locals.filter { it.isNotBlank() }.map { "$it@$d" }
    }

    private fun String.normalize(): String {
        val map = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
        )
        return this.map { map[it] ?: it }.joinToString("").filter { it.isLetterOrDigit() }
    }
}
