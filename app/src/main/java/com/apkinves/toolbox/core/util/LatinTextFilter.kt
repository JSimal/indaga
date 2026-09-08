package com.apkinves.toolbox.core.util

import java.text.Normalizer

/** Reduce un texto a alfabeto latino básico (a-z) + ñ: transcribe letras latinas especiales y quita acentos; si tras eso queda algún carácter no latino (cirílico, griego, CJK...), se descarta. */
object LatinTextFilter {
    private const val ENYE_PLACEHOLDER = ''
    private const val ENYE_MAY_PLACEHOLDER = ''

    private val SPECIAL_LETTERS = mapOf(
        'ß' to "ss", // ß
        'œ' to "oe", // œ
        'Œ' to "OE", // Œ
        'æ' to "ae", // æ
        'Æ' to "AE", // Æ
        'ø' to "o", // ø
        'Ø' to "O", // Ø
        'đ' to "d", // đ
        'Đ' to "D", // Đ
        'ð' to "d", // ð
        'Ð' to "D", // Ð
        'þ' to "th", // þ
        'Þ' to "Th", // Þ
        'ł' to "l", // ł
        'Ł' to "L", // Ł
        'ħ' to "h", // ħ
        'Ħ' to "H", // Ħ
        'ŋ' to "ng", // ŋ
        'Ŋ' to "Ng", // Ŋ
        'ı' to "i", // ı
        'İ' to "I", // İ
    )

    private val ALLOWED_REGEX = Regex("^[A-Za-zÑñ' -]+$")

    /** Devuelve el texto normalizado a alfabeto normal + ñ, o null si contiene caracteres que no son de origen latino (cirílico, griego, CJK...). */
    fun toPlainLatinOrNull(text: String): String? {
        val protected = text
            .replace('ñ', ENYE_PLACEHOLDER) // ñ
            .replace('Ñ', ENYE_MAY_PLACEHOLDER) // Ñ

        val withSpecialsMapped = buildString {
            for (c in protected) append(SPECIAL_LETTERS[c] ?: c.toString())
        }

        val decomposed = Normalizer.normalize(withSpecialsMapped, Normalizer.Form.NFD)
        val withoutMarks = decomposed.replace(Regex("\\p{Mn}+"), "")

        val restored = withoutMarks
            .replace(ENYE_PLACEHOLDER, 'ñ')
            .replace(ENYE_MAY_PLACEHOLDER, 'Ñ')
            .trim()

        return restored.takeIf { it.isNotBlank() && ALLOWED_REGEX.matches(it) }
    }
}
