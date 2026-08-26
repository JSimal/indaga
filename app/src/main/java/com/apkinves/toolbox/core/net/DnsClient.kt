package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.random.Random

/**
 * Cliente DNS minimo sobre UDP (RFC 1035), sin dependencias externas.
 * Usado para A, MX, TXT y NS ya que las APIs de Java no exponen esos tipos.
 */
object DnsClient {

    enum class RecordType(val code: Int) { A(1), NS(2), MX(15), TXT(16), AAAA(28) }

    data class DnsRecord(val type: String, val value: String, val ttl: Long)

    private const val DEFAULT_RESOLVER = "8.8.8.8"
    private const val DNS_PORT = 53
    private const val TIMEOUT_MS = 4000

    /**
     * Intenta primero DNS "de verdad" por UDP/53. Muchas redes móviles y
     * corporativas bloquean ese puerto para forzar su propio DNS; si eso
     * pasa (timeout o error de red), se usa como respaldo DNS-over-HTTPS
     * (puerto 443), que casi nunca está bloqueado.
     */
    suspend fun query(domain: String, type: RecordType, resolver: String = DEFAULT_RESOLVER): List<DnsRecord> {
        return try {
            queryRaw(domain, type, resolver)
        } catch (e: Exception) {
            DohClient.query(domain, type)
        }
    }

    private suspend fun queryRaw(domain: String, type: RecordType, resolver: String): List<DnsRecord> =
        withContext(Dispatchers.IO) {
            val txId = Random.nextInt(0, 65535)
            val request = buildQuery(txId, domain, type)

            DatagramSocket().use { socket ->
                socket.soTimeout = TIMEOUT_MS
                val address = InetAddress.getByName(resolver)
                socket.send(DatagramPacket(request, request.size, address, DNS_PORT))

                val buffer = ByteArray(4096)
                val responsePacket = DatagramPacket(buffer, buffer.size)
                socket.receive(responsePacket)
                parseResponse(buffer, responsePacket.length)
            }
        }

    private fun buildQuery(id: Int, domain: String, type: RecordType): ByteArray {
        val out = ArrayList<Byte>()
        fun u16(v: Int) { out.add(((v shr 8) and 0xFF).toByte()); out.add((v and 0xFF).toByte()) }

        u16(id)
        u16(0x0100) // flags: recursion desired
        u16(1) // qdcount
        u16(0); u16(0); u16(0) // an/ns/ar count

        domain.split(".").forEach { label ->
            out.add(label.length.toByte())
            label.forEach { out.add(it.code.toByte()) }
        }
        out.add(0) // root

        u16(type.code) // qtype
        u16(1) // qclass IN

        return out.toByteArray()
    }

    private fun parseResponse(buffer: ByteArray, length: Int): List<DnsRecord> {
        var pos = 0
        fun u8(): Int = buffer[pos++].toInt() and 0xFF
        fun u16(): Int { val v = ((buffer[pos].toInt() and 0xFF) shl 8) or (buffer[pos + 1].toInt() and 0xFF); pos += 2; return v }
        fun u32(): Long {
            val v = ((buffer[pos].toLong() and 0xFF) shl 24) or ((buffer[pos + 1].toLong() and 0xFF) shl 16) or
                ((buffer[pos + 2].toLong() and 0xFF) shl 8) or (buffer[pos + 3].toLong() and 0xFF)
            pos += 4
            return v
        }

        fun readName(): String {
            val labels = ArrayList<String>()
            while (true) {
                val len = u8()
                if (len == 0) break
                if ((len and 0xC0) == 0xC0) {
                    // pointer: siguiente byte completa el offset, no lo seguimos (no necesario para nuestros tipos)
                    pos++
                    break
                }
                val sb = StringBuilder()
                repeat(len) { sb.append(buffer[pos++].toInt().toChar()) }
                labels.add(sb.toString())
            }
            return labels.joinToString(".")
        }

        pos = 12 // saltar header, ya leimos counts fuera si hiciera falta
        val qdcount = ((buffer[4].toInt() and 0xFF) shl 8) or (buffer[5].toInt() and 0xFF)
        val ancount = ((buffer[6].toInt() and 0xFF) shl 8) or (buffer[7].toInt() and 0xFF)

        repeat(qdcount) {
            readName()
            pos += 4 // qtype + qclass
        }

        val results = ArrayList<DnsRecord>()
        repeat(ancount) {
            readName()
            val type = u16()
            u16() // class
            val ttl = u32()
            val rdlength = u16()
            val rdataStart = pos

            val value = when (type) {
                1 -> { // A
                    "${u8()}.${u8()}.${u8()}.${u8()}"
                }
                28 -> { // AAAA
                    val parts = (0 until 8).map { String.format("%x", u16()) }
                    parts.joinToString(":")
                }
                15 -> { // MX
                    val pref = u16()
                    val exch = readName()
                    "$exch (preferencia $pref)"
                }
                2 -> readName() // NS
                16 -> { // TXT
                    val txtLen = u8()
                    val sb = StringBuilder()
                    repeat(txtLen) { sb.append(buffer[pos++].toInt().toChar()) }
                    sb.toString()
                }
                else -> "(tipo $type no decodificado)"
            }

            pos = rdataStart + rdlength
            val typeName = RecordType.entries.firstOrNull { it.code == type }?.name ?: "TYPE$type"
            results.add(DnsRecord(typeName, value, ttl))
        }
        return results
    }
}
