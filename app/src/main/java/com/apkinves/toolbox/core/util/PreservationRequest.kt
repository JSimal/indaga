package com.apkinves.toolbox.core.util

import com.apkinves.toolbox.data.DirectoryEntry
import com.apkinves.toolbox.data.Firmante

/**
 * Texto de una petición urgente de conservación de datos. Es un BORRADOR:
 * no cita artículos legales concretos a propósito (cada proveedor y cada caso
 * piden una base distinta) y deja entre corchetes lo que falte, para que
 * nunca salga un hueco disfrazado de dato. El agente lo revisa antes de
 * enviarlo.
 */
object PreservationRequest {

    enum class Idioma { ES, EN }

    data class Datos(
        val identificadores: String,
        val referencia: String,
        val hechos: String,
        val firmante: Firmante,
    )

    fun asunto(entrada: DirectoryEntry, d: Datos, idioma: Idioma): String {
        val ref = d.referencia.trim().ifBlank { "[ref.]" }
        return when (idioma) {
            Idioma.EN -> "Preservation request – ${entrada.nombre} – Guardia Civil (Spain) – Ref. $ref"
            Idioma.ES -> "Solicitud de conservación de datos – ${entrada.nombre} – Guardia Civil – Ref. $ref"
        }
    }

    fun texto(entrada: DirectoryEntry, d: Datos, idioma: Idioma): String {
        val ids = d.identificadores.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val f = d.firmante
        fun o(v: String, hueco: String) = v.trim().ifBlank { "[$hueco]" }
        return when (idioma) {
            Idioma.EN -> buildString {
                appendLine("To: ${entrada.nombre} – Law Enforcement Response Team")
                appendLine()
                appendLine("Subject: ${asunto(entrada, d, idioma)}")
                appendLine()
                appendLine("Dear Sir or Madam,")
                appendLine()
                appendLine(
                    "The ${o(f.unidad, "unit")} of the Guardia Civil (Spain) is conducting an official " +
                        "criminal investigation (reference ${o(d.referencia, "reference")}) into " +
                        "${o(d.hechos, "brief description of the offence")}.",
                )
                appendLine()
                appendLine("We respectfully request that you preserve all data associated with the following identifier(s):")
                if (ids.isEmpty()) appendLine("  - [identifier]") else ids.forEach { appendLine("  - $it") }
                appendLine()
                appendLine(
                    "including subscriber and registration information, IP addresses and connection logs, " +
                        "and stored content, for the maximum period your policy allows, pending formal legal " +
                        "process (judicial order, European Investigation Order or MLAT request, as applicable).",
                )
                appendLine()
                appendLine(
                    "Please do not notify the account holder, as this could jeopardise the investigation. " +
                        "We would be grateful if you could confirm the preservation and provide your internal reference number.",
                )
                appendLine()
                appendLine("Yours faithfully,")
                appendLine()
                appendLine(o(f.nombre, "name / ID number"))
                appendLine("Guardia Civil – ${o(f.unidad, "unit")}")
                appendLine(o(f.email, "official email"))
                if (f.telefono.isNotBlank()) appendLine(f.telefono.trim())
            }.trimEnd()

            Idioma.ES -> buildString {
                appendLine("A: ${entrada.nombre} – Departamento de atención a requerimientos policiales")
                appendLine()
                appendLine("Asunto: ${asunto(entrada, d, idioma)}")
                appendLine()
                appendLine("Muy señores míos:")
                appendLine()
                appendLine(
                    "La unidad ${o(f.unidad, "unidad")} de la Guardia Civil instruye diligencias " +
                        "(referencia ${o(d.referencia, "nº de diligencias")}) por " +
                        "${o(d.hechos, "breve descripción de los hechos")}.",
                )
                appendLine()
                appendLine("Se solicita la conservación urgente de todos los datos asociados a los siguientes identificadores:")
                if (ids.isEmpty()) appendLine("  - [identificador]") else ids.forEach { appendLine("  - $it") }
                appendLine()
                appendLine(
                    "incluidos los datos de registro y de abonado, las direcciones IP y los registros de conexión, " +
                        "y el contenido almacenado, durante el máximo plazo que permita su política, a la espera " +
                        "del requerimiento formal (mandamiento judicial, Orden Europea de Investigación o comisión " +
                        "rogatoria, según proceda).",
                )
                appendLine()
                appendLine(
                    "Se ruega no notificar al titular de la cuenta, ya que podría perjudicar la investigación, " +
                        "y confirmar la conservación indicando su número de referencia interno.",
                )
                appendLine()
                appendLine("Atentamente,")
                appendLine()
                appendLine(o(f.nombre, "nombre / TIP"))
                appendLine("Guardia Civil – ${o(f.unidad, "unidad")}")
                appendLine(o(f.email, "email oficial"))
                if (f.telefono.isNotBlank()) appendLine(f.telefono.trim())
            }.trimEnd()
        }
    }
}
