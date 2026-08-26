package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * NOTA IMPORTANTE: un traceroute real necesita fijar el TTL por paquete y
 * leer los mensajes ICMP "TTL exceeded" de cada router intermedio. Java NO
 * expone esa opción para sockets unicast (java.net.StandardSocketOptions
 * solo tiene IP_MULTICAST_TTL, no IP_TTL) y Android no permite raw sockets
 * ICMP sin root. Por eso esto NO es un traceroute hop-by-hop: es un
 * comprobador de alcanzabilidad y latencia hacia el destino final,
 * usando ICMP echo si el sistema lo permite (con fallback a un connect
 * TCP al puerto 7 si no).
 */
object TracerouteClient {

    data class ReachabilityResult(val reachable: Boolean, val rttMs: Long)

    suspend fun probe(host: String, timeoutMs: Int = 4000): ReachabilityResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val reachable = try {
            InetAddress.getByName(host).isReachable(timeoutMs)
        } catch (e: Exception) {
            false
        }
        ReachabilityResult(reachable, System.currentTimeMillis() - start)
    }
}
