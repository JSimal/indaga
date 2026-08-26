package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object PingClient {

    data class PingResult(val attempts: Int, val received: Int, val rttsMs: List<Long>, val avgMs: Double, val lossPercent: Double)

    suspend fun ping(host: String, count: Int = 4, timeoutMs: Int = 3000): Result<PingResult> = withContext(Dispatchers.IO) {
        runCatching {
            val address = InetAddress.getByName(host)
            val rtts = mutableListOf<Long>()
            repeat(count) {
                val start = System.currentTimeMillis()
                val ok = runCatching { address.isReachable(timeoutMs) }.getOrDefault(false)
                if (ok) rtts.add(System.currentTimeMillis() - start)
            }
            val loss = (count - rtts.size) * 100.0 / count
            PingResult(count, rtts.size, rtts, if (rtts.isEmpty()) 0.0 else rtts.average(), loss)
        }
    }
}
