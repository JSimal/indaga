package com.apkinves.toolbox.core.util

import java.math.BigInteger

object Validators {

    fun luhnCheck(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 8) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var d = digits[i] - '0'
            if (alternate) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    data class IbanResult(val valid: Boolean, val countryCode: String, val message: String)

    fun validateIban(rawIban: String): IbanResult {
        val iban = rawIban.replace(" ", "").uppercase()
        if (iban.length < 15 || iban.length > 34) return IbanResult(false, "", "Longitud inválida")
        if (!iban.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$"))) return IbanResult(false, "", "Formato inválido")

        val rearranged = iban.substring(4) + iban.substring(0, 4)
        val numeric = rearranged.map { c -> if (c.isDigit()) c.toString() else (c - 'A' + 10).toString() }.joinToString("")
        val remainder = BigInteger(numeric).mod(BigInteger.valueOf(97))
        val valid = remainder == BigInteger.ONE

        return IbanResult(valid, iban.substring(0, 2), if (valid) "IBAN válido" else "Dígito de control incorrecto")
    }

    data class NifResult(val valid: Boolean, val type: String, val message: String)

    /** Valida NIF/NIE españoles (letra de control) y CIF (dígito/letra de control). */
    fun validateSpanishId(rawId: String): NifResult {
        val id = rawId.trim().uppercase()
        val nifLetters = "TRWAGMYFPDXBNJZSQVHLCKE"

        return when {
            id.matches(Regex("^[0-9]{8}[A-Z]$")) -> {
                val number = id.substring(0, 8).toInt()
                val expected = nifLetters[number % 23]
                val valid = id.last() == expected
                NifResult(valid, "NIF", if (valid) "NIF válido" else "Letra de control incorrecta (esperada: $expected)")
            }
            id.matches(Regex("^[XYZ][0-9]{7}[A-Z]$")) -> {
                val prefix = when (id[0]) { 'X' -> "0"; 'Y' -> "1"; 'Z' -> "2"; else -> "0" }
                val number = (prefix + id.substring(1, 8)).toInt()
                val expected = nifLetters[number % 23]
                val valid = id.last() == expected
                NifResult(valid, "NIE", if (valid) "NIE válido" else "Letra de control incorrecta (esperada: $expected)")
            }
            id.matches(Regex("^[A-HJNPQSUVW][0-9]{7}[0-9A-J]$")) -> {
                val digits = id.substring(1, 8)
                var sumEven = 0
                var sumOdd = 0
                for (i in digits.indices) {
                    val d = digits[i] - '0'
                    if (i % 2 == 0) {
                        val doubled = d * 2
                        sumOdd += if (doubled > 9) doubled - 9 else doubled
                    } else {
                        sumEven += d
                    }
                }
                val total = sumEven + sumOdd
                val controlDigit = (10 - (total % 10)) % 10
                val controlLetter = "JABCDEFGHI"[controlDigit]
                val last = id.last()
                val valid = last == controlLetter.toString()[0] || last == ('0' + controlDigit)
                NifResult(valid, "CIF", if (valid) "CIF válido" else "Dígito/letra de control incorrecto (esperado: $controlDigit o $controlLetter)")
            }
            else -> NifResult(false, "Desconocido", "Formato no reconocido como NIF/NIE/CIF español")
        }
    }
}
