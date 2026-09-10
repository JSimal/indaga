package com.apkinves.toolbox.core.util

/** Extrae enlaces, correos y teléfonos de un texto pegado (artículos, capturas, páginas raspadas...). */
object TextExtractor {
    data class Extraction(val urls: List<String>, val emails: List<String>, val phones: List<String>)

    private val URL_REGEX = Regex("""(https?://[^\s"'<>()\[\]]+)""", RegexOption.IGNORE_CASE)
    private val EMAIL_REGEX = Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""")
    private val PHONE_REGEX = Regex("""(?<![\d.])(\+?\d[\d ().-]{7,}\d)(?![\d.])""")

    fun extract(text: String): Extraction {
        val urls = URL_REGEX.findAll(text).map { it.value.trimEnd('.', ',', ')') }.distinct().toList()
        val emails = EMAIL_REGEX.findAll(text).map { it.value }.distinct().toList()
        val phones = PHONE_REGEX.findAll(text)
            .map { it.value.trim() }
            .filter { it.filter(Char::isDigit).length in 7..15 }
            .distinct()
            .toList()
        return Extraction(urls, emails, phones)
    }
}
