package com.apkinves.toolbox.core.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object WifiScanner {

    data class ApInfo(val ssid: String, val bssid: String, val level: Int, val capabilities: String, val frequency: Int)

    /**
     * Lanza un escaneo y espera a que el sistema confirme que ha terminado
     * (broadcast SCAN_RESULTS_AVAILABLE_ACTION) antes de leer resultados,
     * con un margen de espera por si Android limita la frecuencia de
     * escaneos de esta app (~4 cada 2 minutos desde Android 9): en ese caso
     * se devuelven igualmente los últimos resultados que tenga el sistema.
     */
    suspend fun scanResults(context: Context): List<ApInfo> = withContext(Dispatchers.IO) {
        val wifiManager = context.applicationContext.getSystemService<WifiManager>() ?: return@withContext emptyList()

        val started = runCatching { wifiManager.startScan() }.getOrDefault(false)
        if (started) {
            withTimeoutOrNull(8000) { awaitScanFinished(context) }
        }

        runCatching {
            wifiManager.scanResults.map {
                ApInfo(
                    ssid = it.SSID.ifBlank { "(oculto)" },
                    bssid = it.BSSID ?: "",
                    level = it.level,
                    capabilities = it.capabilities ?: "",
                    frequency = it.frequency,
                )
            }
        }.getOrDefault(emptyList())
    }

    private suspend fun awaitScanFinished(context: Context) = suspendCancellableCoroutine<Unit> { cont ->
        lateinit var receiver: BroadcastReceiver
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                runCatching { context.unregisterReceiver(receiver) }
                if (cont.isActive) cont.resume(Unit)
            }
        }
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        cont.invokeOnCancellation { runCatching { context.unregisterReceiver(receiver) } }
    }

    /** Heurística simple de "evil twin": mismo SSID con varios BSSID cuya seguridad difiere. */
    fun detectSuspicious(networks: List<ApInfo>): List<String> {
        val bySsid = networks.groupBy { it.ssid }
        val warnings = mutableListOf<String>()
        bySsid.forEach { (ssid, list) ->
            if (ssid == "(oculto)" || list.size < 2) return@forEach
            val securities = list.map { extractSecurity(it.capabilities) }.distinct()
            if (securities.size > 1) {
                warnings.add("'$ssid' aparece con ${list.size} puntos de acceso y seguridad distinta entre ellos (${securities.joinToString(" / ")}) — posible red gemela/evil twin.")
            }
        }
        return warnings
    }

    private fun extractSecurity(capabilities: String): String = when {
        capabilities.contains("WPA3") -> "WPA3"
        capabilities.contains("WPA2") -> "WPA2"
        capabilities.contains("WPA") -> "WPA"
        capabilities.contains("WEP") -> "WEP"
        else -> "Abierta"
    }
}
