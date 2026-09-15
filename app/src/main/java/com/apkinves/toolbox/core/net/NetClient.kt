package com.apkinves.toolbox.core.net

import com.apkinves.toolbox.ui.ProxyPreference
import java.net.HttpURLConnection
import java.net.URL

/**
 * Punto único para abrir conexiones HTTP en toda la app: si el usuario ha
 * activado un proxy SOCKS5 (Ajustes → Proxy/Tor), todas las consultas de red
 * pasan por él en vez de salir directamente con la IP del dispositivo.
 */
object NetClient {
    fun openConnection(urlStr: String): HttpURLConnection =
        URL(urlStr).openConnection(ProxyPreference.currentProxy()) as HttpURLConnection

    fun openConnection(url: URL): HttpURLConnection =
        url.openConnection(ProxyPreference.currentProxy()) as HttpURLConnection
}
