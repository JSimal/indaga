package com.apkinves.toolbox.core.net

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Barrido de la subred local (asume /24, lo habitual en redes domésticas) por
 * ICMP/TCP de bajo coste. No necesita permisos peligrosos: solo el estado de
 * WiFi (para saber tu propia IP) y red, ambos permisos "normales".
 */
object LocalNetworkScanner {

    data class Device(val ip: String, val rttMs: Long)

    fun ownIpAndPrefix(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        @Suppress("DEPRECATION")
        val ipInt = wifiManager.connectionInfo?.ipAddress ?: return null
        if (ipInt == 0) return null
        val bytes = intArrayOf(
            ipInt and 0xFF,
            (ipInt shr 8) and 0xFF,
            (ipInt shr 16) and 0xFF,
            (ipInt shr 24) and 0xFF,
        )
        return "${bytes[0]}.${bytes[1]}.${bytes[2]}"
    }

    // Limita cuántas conexiones van a la vez: 254 sockets simultáneos de golpe
    // puede notarse en un móvil de gama baja sin aportar nada (el cuello de
    // botella real es la red, no la CPU).
    private const val MAX_CONCURRENT = 32

    suspend fun scan(subnetPrefix: String, timeoutMs: Int = 400): List<Device> = withContext(Dispatchers.IO) {
        val semaphore = Semaphore(MAX_CONCURRENT)
        (1..254).map { host ->
            async {
                semaphore.withPermit {
                    val ip = "$subnetPrefix.$host"
                    val start = System.currentTimeMillis()
                    val reachable = runCatching { InetAddress.getByName(ip).isReachable(timeoutMs) }.getOrDefault(false)
                    if (reachable) Device(ip, System.currentTimeMillis() - start) else null
                }
            }
        }.awaitAll().filterNotNull().sortedBy { it.ip }
    }
}
