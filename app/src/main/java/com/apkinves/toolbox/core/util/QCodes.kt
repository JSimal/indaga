package com.apkinves.toolbox.core.util

/** Código Q internacional de radioaficionado/radiotelegrafía: los más usados en la práctica. */
object QCodes {
    data class QCode(val code: String, val question: String, val statement: String)

    val CODES = listOf(
        QCode("QRA", "¿Cuál es el nombre de su estación?", "El nombre de mi estación es..."),
        QCode("QRG", "¿Cuál es mi frecuencia exacta?", "Su frecuencia exacta es..."),
        QCode("QRH", "¿Varía mi frecuencia?", "Su frecuencia varía"),
        QCode("QRI", "¿Cómo es el tono de mi señal?", "El tono de su señal es..."),
        QCode("QRK", "¿Cuál es la inteligibilidad de mis señales?", "La inteligibilidad de sus señales es... (1-5)"),
        QCode("QRL", "¿Está ocupada la frecuencia?", "La frecuencia está ocupada, por favor no interfiera"),
        QCode("QRM", "¿Sufre interferencia de otras estaciones?", "Sufro interferencia de otras estaciones"),
        QCode("QRN", "¿Le molestan los parásitos atmosféricos?", "Me molestan los parásitos atmosféricos"),
        QCode("QRO", "¿Debo aumentar la potencia?", "Aumente la potencia"),
        QCode("QRP", "¿Debo disminuir la potencia?", "Disminuya la potencia (también: estación de baja potencia)"),
        QCode("QRQ", "¿Debo transmitir más rápido?", "Transmita más rápido"),
        QCode("QRS", "¿Debo transmitir más despacio?", "Transmita más despacio"),
        QCode("QRT", "¿Debo cesar la transmisión?", "Cese la transmisión"),
        QCode("QRU", "¿Tiene algo para mí?", "No tengo nada para usted"),
        QCode("QRV", "¿Está listo?", "Estoy listo"),
        QCode("QRX", "¿Cuándo me volverá a llamar?", "Le volveré a llamar a las... horas"),
        QCode("QRZ", "¿Quién me llama?", "Le llama..."),
        QCode("QSA", "¿Cuál es la intensidad de mis señales?", "La intensidad de sus señales es... (1-5)"),
        QCode("QSB", "¿Se desvanecen mis señales?", "Sus señales se desvanecen (fading)"),
        QCode("QSL", "¿Puede confirmar recepción?", "Confirmo recepción (tarjeta/acuse de recibo)"),
        QCode("QSO", "¿Puede comunicar con...?", "Puedo comunicar con... (una conversación radial)"),
        QCode("QSP", "¿Puede retransmitir a...?", "Puedo retransmitir a..."),
        QCode("QSY", "¿Debo cambiar de frecuencia?", "Cambie de frecuencia a..."),
        QCode("QTH", "¿Cuál es su ubicación?", "Mi ubicación es..."),
        QCode("QTR", "¿Qué hora es?", "La hora exacta es..."),
    )

    fun search(query: String): List<QCode> {
        if (query.isBlank()) return CODES
        return CODES.filter {
            it.code.contains(query, ignoreCase = true) ||
                it.question.contains(query, ignoreCase = true) ||
                it.statement.contains(query, ignoreCase = true)
        }
    }
}
