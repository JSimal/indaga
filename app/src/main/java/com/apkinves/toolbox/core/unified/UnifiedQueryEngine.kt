package com.apkinves.toolbox.core.unified

import com.apkinves.toolbox.core.net.AsnLookupClient
import com.apkinves.toolbox.core.net.DnsClient
import com.apkinves.toolbox.core.net.HostingPatternDetector
import com.apkinves.toolbox.core.net.IpInfo
import com.apkinves.toolbox.core.net.IpInfoClient
import com.apkinves.toolbox.core.net.PhishingFeedClient
import com.apkinves.toolbox.core.net.PortScanner
import com.apkinves.toolbox.core.net.RdapClient
import com.apkinves.toolbox.core.net.RdapSummary
import com.apkinves.toolbox.core.net.SslCertClient
import com.apkinves.toolbox.core.net.WhoisClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

enum class InputKind { IP, DOMAIN, UNKNOWN }

private val ipRegex = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

fun detectKind(value: String): InputKind = when {
    ipRegex.matches(value.trim()) -> InputKind.IP
    value.contains(".") && !value.contains(" ") -> InputKind.DOMAIN
    else -> InputKind.UNKNOWN
}

data class UnifiedReport(
    val target: String,
    val kind: InputKind,
    val rdap: RdapSummary?,
    val ipInfo: IpInfo?,
    val dnsRecords: Map<DnsClient.RecordType, List<DnsClient.DnsRecord>>,
    val openPorts: List<PortScanner.PortResult>,
    val rawWhois: String,
    val asnInfo: AsnLookupClient.AsnInfo?,
    val hostingPattern: String?,
    val sslCerts: List<SslCertClient.CertInfo>?,
    val phishingMatch: Boolean?,
)

/** Lógica compartida entre la Consulta Única y la Consulta por Lotes. */
object UnifiedQueryEngine {

    suspend fun run(value: String, kind: InputKind): UnifiedReport = coroutineScope {
        val rdapJob = async {
            runCatching {
                if (kind == InputKind.IP) RdapClient.lookupIpStructured(value) else RdapClient.lookupDomainStructured(value)
            }.getOrNull()?.getOrNull()
        }
        val rawWhoisJob = async {
            runCatching { if (kind == InputKind.IP) WhoisClient.lookupIp(value) else WhoisClient.lookupDomain(value) }
                .getOrElse { "Error: ${it.message}" }
        }

        // Para dominios, la IP a inspeccionar (IP info / puertos) es la que resuelva el registro A.
        val ipForChecks = if (kind == InputKind.IP) value else {
            runCatching { DnsClient.query(value, DnsClient.RecordType.A) }.getOrNull()?.firstOrNull()?.value
        }

        val ipInfoJob = async {
            ipForChecks?.let { runCatching { IpInfoClient.lookup(it) }.getOrNull() }
        }
        val portsJob = async {
            runCatching { PortScanner.scan(ipForChecks ?: value) }.getOrElse { emptyList() }
        }

        val dnsTypes = listOf(DnsClient.RecordType.A, DnsClient.RecordType.AAAA, DnsClient.RecordType.CNAME, DnsClient.RecordType.MX, DnsClient.RecordType.NS, DnsClient.RecordType.TXT)
        val dnsRecords: Map<DnsClient.RecordType, List<DnsClient.DnsRecord>> = if (kind == InputKind.DOMAIN) {
            dnsTypes.associateWith { type -> runCatching { DnsClient.query(value, type) }.getOrElse { emptyList() } }
        } else emptyMap()

        val asnJob = async {
            ipForChecks?.let { runCatching { AsnLookupClient.lookup(it) }.getOrNull() }
        }

        val cnameTargets = dnsRecords[DnsClient.RecordType.CNAME]?.map { it.value } ?: emptyList()
        val hostingPattern = HostingPatternDetector.detect(cnameTargets)

        val sslJob = async {
            runCatching { SslCertClient.inspect(value) }.getOrNull()?.getOrNull()
        }
        val phishingJob = async {
            if (kind == InputKind.DOMAIN) runCatching { PhishingFeedClient.checkDomain(value) }.getOrNull()?.getOrNull() else null
        }

        UnifiedReport(
            target = value,
            kind = kind,
            rdap = rdapJob.await(),
            ipInfo = ipInfoJob.await(),
            dnsRecords = dnsRecords,
            openPorts = portsJob.await(),
            rawWhois = rawWhoisJob.await(),
            asnInfo = asnJob.await(),
            hostingPattern = hostingPattern,
            sslCerts = sslJob.await(),
            phishingMatch = phishingJob.await(),
        )
    }

    fun buildRawSummary(r: UnifiedReport): String = buildString {
        appendLine("== ${r.target} ==")
        appendLine("-- WHOIS/RDAP --")
        r.rdap?.let {
            appendLine("Registrador: ${it.registrar ?: "?"}")
            appendLine("Alta: ${it.registeredDate ?: "?"}  Expira: ${it.expiresDate ?: "?"}")
        }
        appendLine(r.rawWhois.take(600))
        appendLine("-- IP Info --")
        r.ipInfo?.let { appendLine("${it.country} · ${it.isp} · proxy=${it.proxy} hosting=${it.hosting}") }
        r.asnInfo?.let { appendLine("ASN AS${it.asn} (${it.name ?: "?"}) · prefijo ${it.bgpPrefix ?: "?"}") }
        r.hostingPattern?.let { appendLine("Plataforma detectada por CNAME: $it") }
        r.phishingMatch?.let { appendLine(if (it) "⚠ Aparece en el feed de phishing de OpenPhish" else "No aparece en el feed de phishing de OpenPhish") }
        r.sslCerts?.firstOrNull()?.let { appendLine("Certificado SSL: ${it.subject} (expira en ${it.daysUntilExpiry} días)") }
        appendLine("-- DNS --")
        r.dnsRecords.forEach { (type, records) -> appendLine("$type: ${records.joinToString { rec -> rec.value }}") }
        appendLine("-- Puertos abiertos --")
        appendLine(r.openPorts.filter { it.open }.joinToString(", ") { "${it.port}(${it.service})" })
    }
}
