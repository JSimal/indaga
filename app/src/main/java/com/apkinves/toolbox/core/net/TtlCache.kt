package com.apkinves.toolbox.core.net

import java.util.concurrent.ConcurrentHashMap

/**
 * Caché en memoria genérica con expiración por clave, para no repetir
 * peticiones de red idénticas en poco tiempo (ej. varias entradas de la
 * misma URL en una Consulta por Lotes, o varias comprobaciones del mismo
 * servicio en el panel de Estado de servicios).
 *
 * Sin mutex global a propósito: cada clave se resuelve de forma
 * independiente para no serializar comprobaciones en paralelo de servicios
 * distintos. Una duplicación puntual de la petición de red bajo carrera es
 * un coste aceptable frente a bloquear todas las claves entre sí.
 */
class TtlCache<K, V>(private val ttlMs: Long) {
    private val entries = ConcurrentHashMap<K, Pair<Long, V>>()

    suspend fun getOrFetch(key: K, fetch: suspend () -> V): V {
        val now = System.currentTimeMillis()
        entries[key]?.let { (cachedAt, value) -> if (now - cachedAt < ttlMs) return value }
        val fresh = fetch()
        entries[key] = now to fresh
        return fresh
    }
}
