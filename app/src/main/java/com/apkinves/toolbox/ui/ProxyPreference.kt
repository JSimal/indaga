package com.apkinves.toolbox.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * Proxy SOCKS5 opcional para las consultas de red (pensado para Orbot, que
 * expone Tor como SOCKS5 en 127.0.0.1:9050 por defecto). No es una
 * integración con Tor en sí — Orbot (u otro proxy SOCKS5) se instala y
 * arranca aparte; esto solo enruta las peticiones HTTP de la app a través
 * de él si el usuario lo activa.
 */
object ProxyPreference {
    private const val PREFS = "proxy_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOST = "host"
    private const val KEY_PORT = "port"

    private const val DEFAULT_HOST = "127.0.0.1"
    private const val DEFAULT_PORT = 9050

    var enabled by mutableStateOf(false)
        private set
    var host by mutableStateOf(DEFAULT_HOST)
        private set
    var port by mutableStateOf(DEFAULT_PORT)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabled = prefs.getBoolean(KEY_ENABLED, false)
        host = prefs.getString(KEY_HOST, DEFAULT_HOST) ?: DEFAULT_HOST
        port = prefs.getInt(KEY_PORT, DEFAULT_PORT)
    }

    fun setEnabled(context: Context, value: Boolean) {
        enabled = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun setHostPort(context: Context, newHost: String, newPort: Int) {
        host = newHost
        port = newPort
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_HOST, newHost)
            .putInt(KEY_PORT, newPort)
            .apply()
    }

    /** Proxy actual a usar en las conexiones de red, o [Proxy.NO_PROXY] si está desactivado. */
    fun currentProxy(): Proxy =
        if (enabled) Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port)) else Proxy.NO_PROXY
}
