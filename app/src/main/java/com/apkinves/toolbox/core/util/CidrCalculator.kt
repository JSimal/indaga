package com.apkinves.toolbox.core.util

object CidrCalculator {

    data class CidrResult(
        val networkAddress: String,
        val broadcastAddress: String,
        val firstHost: String,
        val lastHost: String,
        val totalHosts: Long,
        val usableHosts: Long,
        val subnetMask: String,
    )

    fun calculate(cidr: String): Result<CidrResult> = runCatching {
        val (ipStr, prefixStr) = cidr.trim().split("/").let {
            require(it.size == 2) { "Formato esperado: 192.168.1.0/24" }
            it[0] to it[1]
        }
        val prefix = prefixStr.toInt()
        require(prefix in 0..32) { "El prefijo debe estar entre 0 y 32" }

        val ipLong = ipToLong(ipStr)
        val maskLong = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL

        val network = ipLong and maskLong
        val broadcast = network or (maskLong.inv() and 0xFFFFFFFFL)

        val total = 1L shl (32 - prefix)
        val usable = if (prefix >= 31) total else total - 2

        CidrResult(
            networkAddress = longToIp(network),
            broadcastAddress = longToIp(broadcast),
            firstHost = if (prefix >= 31) longToIp(network) else longToIp(network + 1),
            lastHost = if (prefix >= 31) longToIp(broadcast) else longToIp(broadcast - 1),
            totalHosts = total,
            usableHosts = usable,
            subnetMask = longToIp(maskLong),
        )
    }

    private fun ipToLong(ip: String): Long {
        val parts = ip.trim().split(".")
        require(parts.size == 4) { "IP inválida" }
        return parts.fold(0L) { acc, part ->
            val v = part.toInt()
            require(v in 0..255) { "Octeto fuera de rango" }
            (acc shl 8) or v.toLong()
        }
    }

    private fun longToIp(value: Long): String {
        return listOf(24, 16, 8, 0).joinToString(".") { shift -> ((value shr shift) and 0xFF).toString() }
    }
}
