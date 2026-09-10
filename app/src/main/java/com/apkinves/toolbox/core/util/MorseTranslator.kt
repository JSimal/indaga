package com.apkinves.toolbox.core.util

/** Traductor Morse ↔ texto (letras, números y signos de puntuación básicos). */
object MorseTranslator {
    private val TEXT_TO_MORSE = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.", '!' to "-.-.--",
        '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-", '&' to ".-...", ':' to "---...",
        ';' to "-.-.-.", '=' to "-...-", '+' to ".-.-.", '-' to "-....-", '_' to "..--.-",
        '"' to ".-..-.", '$' to "...-..-", '@' to ".--.-.",
    )

    private val MORSE_TO_TEXT = TEXT_TO_MORSE.entries.associate { (k, v) -> v to k }

    fun textToMorse(text: String): String =
        text.uppercase().map { c ->
            if (c == ' ') "/" else TEXT_TO_MORSE[c] ?: "?"
        }.joinToString(" ")

    fun morseToText(morse: String): String =
        morse.trim().split(Regex("\\s+")).joinToString("") { token ->
            if (token == "/") " " else MORSE_TO_TEXT[token]?.toString() ?: "�"
        }
}
