package com.apkinves.toolbox.core.util

import com.apkinves.toolbox.data.CaseEntry

/**
 * Correlación básica entre entradas del historial: extrae IPs y dominios
 * mencionados en cada consulta (el objetivo original y el texto del
 * resultado guardado) y agrupa las entradas que comparten una misma entidad.
 * No es un grafo completo (sin líneas de tiempo ni tipos de relación), pero
 * permite ver de un vistazo qué consultas distintas tocan el mismo dominio/IP.
 */
object EntityCorrelator {

    private val ipRegex = Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")
    private val domainRegex = Regex("\\b(?:[a-z0-9-]+\\.)+[a-z]{2,}\\b", RegexOption.IGNORE_CASE)

    fun extractEntities(entry: CaseEntry): Set<String> {
        val text = "${entry.target}\n${entry.fullResult}"
        val ips = ipRegex.findAll(text).map { it.value }.toSet()
        val domains = domainRegex.findAll(text).map { it.value.lowercase().removePrefix("www.") }.toSet()
        return ips + domains
    }

    /** Entidad -> entradas del historial que la mencionan, solo para entidades presentes en 2 o más entradas distintas. */
    fun correlate(entries: List<CaseEntry>): Map<String, List<CaseEntry>> {
        val byEntity = mutableMapOf<String, MutableList<CaseEntry>>()
        entries.forEach { entry ->
            extractEntities(entry).forEach { entity ->
                byEntity.getOrPut(entity) { mutableListOf() }.add(entry)
            }
        }
        return byEntity
            .filterValues { list -> list.map { it.id }.distinct().size > 1 }
            .toList()
            .sortedByDescending { it.second.size }
            .toMap()
    }
}
