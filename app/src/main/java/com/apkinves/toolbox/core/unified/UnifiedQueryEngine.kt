package com.apkinves.toolbox.core.unified

import com.apkinves.toolbox.core.net.AsnLookupClient
import com.apkinves.toolbox.core.net.BannerGrabber
import com.apkinves.toolbox.core.net.BlacklistChecker
import com.apkinves.toolbox.core.net.DnsClient
import com.apkinves.toolbox.core.net.EmailSecurityClient
import com.apkinves.toolbox.core.net.HostingPatternDetector
import com.apkinves.toolbox.core.net.IpInfo
import com.apkinves.toolbox.core.net.IpInfoClient
import com.apkinves.toolbox.core.net.MetaExtractor
import com.apkinves.toolbox.core.net.PhishingFeedClient
import com.apkinves.toolbox.core.net.PortScanner
import com.apkinves.toolbox.core.net.RdapClient
import com.apkinves.toolbox.core.net.RdapSummary
import com.apkinves.toolbox.core.net.RedirectChecker
import com.apkinves.toolbox.core.net.SiteFilesClient
import com.apkinves.toolbox.core.net.SslCertClient
import com.apkinves.toolbox.core.net.SubdomainFinder
import com.apkinves.toolbox.core.net.TechDetector
import com.apkinves.toolbox.core.net.UptimeChecker
import com.apkinves.toolbox.core.net.WaybackClient
import com.apkinves.toolbox.core.net.WaybackSnapshot
import com.apkinves.toolbox.core.net.WhoisClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    val reverseDns: String?,
    val portBanners: Map<Int, String>,
    val blacklistResults: List<BlacklistChecker.BlacklistResult>,
    val uptime: UptimeChecker.UptimeResult?,
    val emailSecurity: EmailSecurityClient.EmailSecurityReport?,
    val robotsTxt: String?,
    val sitemapFound: Boolean?,
    val metaReport: MetaExtractor.MetaReport?,
    val subdomains: List<String>,
    val techReport: TechDetector.TechReport?,
    val redirectHops: List<RedirectChecker.Hop>,
    val waybackSnapshot: WaybackSnapshot?,
    val ipInfoError: String?,
)

/** Lógica compartida entre la Consulta Única y la Consulta por Lotes. */
object UnifiedQueryEngine {

    suspend fun run(value: String, kind: InputKind): UnifiedReport = coroutineScope {
        val isDomain = kind == InputKind.DOMAIN

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

        // Se distingue el motivo del fallo (útil porque tiene causas muy
        // distintas: DNS que no resuelve, timeout de red, límite gratuito de
        // la API superado, o IP privada/reservada sin datos de hosting).
        val ipInfoResultJob = async {
            if (ipForChecks == null) null else runCatching { IpInfoClient.lookup(ipForChecks) }
        }
        val portsJob = async {
            runCatching { PortScanner.scan(ipForChecks ?: value) }.getOrElse { emptyList() }
        }

        val dnsTypes = listOf(DnsClient.RecordType.A, DnsClient.RecordType.AAAA, DnsClient.RecordType.CNAME, DnsClient.RecordType.MX, DnsClient.RecordType.NS, DnsClient.RecordType.TXT)
        val dnsRecords: Map<DnsClient.RecordType, List<DnsClient.DnsRecord>> = if (isDomain) {
            dnsTypes.associateWith { type -> runCatching { DnsClient.query(value, type) }.getOrElse { emptyList() } }
        } else emptyMap()

        val asnJob = async {
            ipForChecks?.let { runCatching { AsnLookupClient.lookup(it) }.getOrNull() }
        }
        val reverseDnsJob = async {
            ipForChecks?.let { runCatching { DnsClient.reverseLookup(it) }.getOrNull() }
        }

        val sslJob = async {
            runCatching { SslCertClient.inspect(value) }.getOrNull()?.getOrNull()
        }
        val phishingJob = async {
            if (isDomain) runCatching { PhishingFeedClient.checkDomain(value) }.getOrNull()?.getOrNull() else null
        }

        // --- Fusión "barata" ---
        val blacklistJob = async {
            ipForChecks?.let { runCatching { BlacklistChecker.check(it) }.getOrElse { emptyList() } } ?: emptyList()
        }
        val uptimeJob = async {
            if (isDomain) runCatching { UptimeChecker.check(value) }.getOrNull() else null
        }
        val emailSecJob = async {
            if (isDomain) runCatching { EmailSecurityClient.analyze(value) }.getOrNull() else null
        }
        val robotsJob = async {
            if (isDomain) runCatching { SiteFilesClient.fetch(value, "robots.txt") }.getOrNull() else null
        }
        val sitemapJob = async {
            if (isDomain) runCatching { SiteFilesClient.fetch(value, "sitemap.xml") }.getOrNull()?.found else null
        }
        val metaJob = async {
            if (isDomain) runCatching { MetaExtractor.extract(value) }.getOrNull()?.getOrNull() else null
        }

        // --- Fusión "razonable" ---
        val subdomainsJob = async {
            if (isDomain) runCatching { SubdomainFinder.find(value) }.getOrElse { emptyList() } else emptyList()
        }
        val techJob = async {
            if (isDomain) runCatching { TechDetector.detect(value) }.getOrNull()?.getOrNull() else null
        }
        val redirectsJob = async {
            if (isDomain) runCatching { RedirectChecker.trace(value) }.getOrElse { emptyList() } else emptyList()
        }
        val waybackJob = async {
            if (isDomain) runCatching { WaybackClient.closestSnapshot(value) }.getOrNull()?.getOrNull() else null
        }

        val openPorts = portsJob.await()
        val host = ipForChecks ?: value
        val bannerJobs = openPorts.filter { it.open }.map { port ->
            async { port.port to BannerGrabber.grab(host, port.port) }
        }
        val portBanners = bannerJobs.awaitAll().mapNotNull { (port, banner) -> banner?.let { port to it } }.toMap()

        val cnameTargets = dnsRecords[DnsClient.RecordType.CNAME]?.map { it.value } ?: emptyList()
        val hostingPattern = HostingPatternDetector.detect(cnameTargets)

        val ipInfoResult = ipInfoResultJob.await()
        val ipInfoRaw = ipInfoResult?.getOrNull()
        val ipInfo = ipInfoRaw?.takeIf { it.status != "fail" }
        val ipInfoError = when {
            ipForChecks == null -> "No se pudo resolver la IP del dominio (sin registro A, o el DNS no respondió)."
            ipInfoResult != null && ipInfoResult.isFailure -> "Fallo al consultar la API de hosting: ${ipInfoResult.exceptionOrNull()?.message ?: "error de red o timeout"}."
            ipInfoRaw != null && ipInfoRaw.status == "fail" -> "La IP no tiene datos de hosting disponibles${ipInfoRaw.message.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""} — probablemente es una IP privada o reservada."
            else -> null
        }

        UnifiedReport(
            target = value,
            kind = kind,
            rdap = rdapJob.await(),
            ipInfo = ipInfo,
            dnsRecords = dnsRecords,
            openPorts = openPorts,
            rawWhois = rawWhoisJob.await(),
            asnInfo = asnJob.await(),
            hostingPattern = hostingPattern,
            sslCerts = sslJob.await(),
            phishingMatch = phishingJob.await(),
            reverseDns = reverseDnsJob.await(),
            portBanners = portBanners,
            blacklistResults = blacklistJob.await(),
            uptime = uptimeJob.await(),
            emailSecurity = emailSecJob.await(),
            robotsTxt = robotsJob.await()?.let { if (it.found) it.content.take(1000) else null },
            sitemapFound = sitemapJob.await(),
            metaReport = metaJob.await(),
            subdomains = subdomainsJob.await(),
            techReport = techJob.await(),
            redirectHops = redirectsJob.await(),
            waybackSnapshot = waybackJob.await(),
            ipInfoError = ipInfoError,
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
            ?: r.ipInfoError?.let { appendLine(it) }
        r.asnInfo?.let { appendLine("ASN AS${it.asn} (${it.name ?: "?"}) · prefijo ${it.bgpPrefix ?: "?"}") }
        r.hostingPattern?.let { appendLine("Plataforma detectada por CNAME: $it") }
        r.phishingMatch?.let { appendLine(if (it) "⚠ Aparece en el feed de phishing de OpenPhish" else "No aparece en el feed de phishing de OpenPhish") }
        r.sslCerts?.firstOrNull()?.let { appendLine("Certificado SSL: ${it.subject} (expira en ${it.daysUntilExpiry} días)") }
        r.reverseDns?.let { appendLine("DNS inverso (PTR): $it") }
        if (r.blacklistResults.any { it.listed }) appendLine("⚠ En listas negras: ${r.blacklistResults.filter { it.listed }.joinToString { it.zone }}")
        r.uptime?.let { appendLine("Disponibilidad: ${if (it.up) "arriba" else "caído"} (${it.statusCode}, ${it.rttMs}ms)") }
        r.emailSecurity?.let { appendLine("Seguridad email: nota ${it.grade}") }
        r.sitemapFound?.let { appendLine("sitemap.xml: ${if (it) "encontrado" else "no encontrado"}") }
        r.techReport?.let { if (it.detected.isNotEmpty()) appendLine("Tecnologías: ${it.detected.joinToString(", ")}") }
        if (r.subdomains.isNotEmpty()) appendLine("Subdominios (${r.subdomains.size}): ${r.subdomains.take(10).joinToString(", ")}")
        r.waybackSnapshot?.let { appendLine("Wayback: copia archivada en ${it.timestamp}") }
        appendLine("-- DNS --")
        r.dnsRecords.forEach { (type, records) -> appendLine("$type: ${records.joinToString { rec -> rec.value }}") }
        appendLine("-- Puertos abiertos --")
        r.openPorts.filter { it.open }.forEach { p ->
            val banner = r.portBanners[p.port]?.let { " → $it" } ?: ""
            appendLine("${p.port}(${p.service})$banner")
        }
        if (r.redirectHops.size > 1) {
            appendLine("-- Redirecciones --")
            r.redirectHops.forEach { appendLine("[${it.statusCode}] ${it.url}") }
        }
    }
}
