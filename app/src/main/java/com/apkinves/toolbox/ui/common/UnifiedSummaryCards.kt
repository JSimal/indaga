package com.apkinves.toolbox.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.unified.UnifiedReport
import com.apkinves.toolbox.ui.theme.CyberColors

@Composable
fun UnifiedSummaryCards(r: UnifiedReport) {
    InfoCard(title = "📋 Registro del dominio/IP") {
        val rdap = r.rdap
        if (rdap == null) {
            Text("No se pudo obtener información estructurada (puede seguir disponible en el WHOIS en crudo).", style = MaterialTheme.typography.bodySmall)
        } else {
            InfoRow("Registrador", rdap.registrar)
            InfoRow("Nombre/Handle", rdap.name ?: rdap.handle)
            InfoRow("País", rdap.country)
            InfoRow("Fecha de alta", rdap.registeredDate?.take(10))
            InfoRow("Fecha de expiración", rdap.expiresDate?.take(10))
            InfoRow("Última modificación", rdap.lastChangedDate?.take(10))
            if (rdap.statuses.isNotEmpty()) InfoRow("Estado", rdap.statuses.joinToString(", "))
            if (rdap.nameservers.isNotEmpty()) InfoRow("Servidores DNS", rdap.nameservers.joinToString("\n"))
        }
    }

    InfoCard(title = "🖥️ Hosting y red") {
        val info = r.ipInfo
        if (info == null) {
            Text("Sin datos de IP disponibles.", style = MaterialTheme.typography.bodySmall)
        } else {
            if (r.hostingPattern != null) {
                Text("Plataforma detectada: ${r.hostingPattern}", style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonGreen, fontWeight = FontWeight.Bold)
            }
            InfoRow("IP", info.query)
            InfoRow("Organización / hosting", info.org.ifBlank { info.isp })
            InfoRow("ISP", info.isp)
            r.asnInfo?.let { InfoRow("ASN", "AS${it.asn}${it.name?.let { n -> " — $n" } ?: ""}") }
            InfoRow("País", "${info.country} (${info.countryCode})")
            InfoRow("Ciudad", info.city)
            InfoRow("Código postal", info.zip)
            if (info.lat != 0.0 || info.lon != 0.0) InfoRow("Coordenadas", "${info.lat}, ${info.lon}")
            InfoRow("Zona horaria", info.timezone)
            InfoRow("DNS inverso (PTR)", r.reverseDns)
            val flags = buildList {
                if (info.proxy) add("VPN/Proxy detectado")
                if (info.hosting) add("IP de datacenter/hosting")
                if (info.mobile) add("Red móvil")
            }
            if (flags.isNotEmpty()) {
                Text(flags.joinToString("  •  "), style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonAmber, fontWeight = FontWeight.Bold)
            } else {
                Text("Sin indicios de VPN/proxy/hosting.", style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonGreen)
            }
        }
    }

    if (r.dnsRecords.isNotEmpty()) {
        InfoCard(title = "🌐 Registros DNS") {
            r.dnsRecords.forEach { (type, records) ->
                InfoRow(type.name, if (records.isEmpty()) "(sin registros)" else records.joinToString("\n") { it.value })
            }
        }
    }

    InfoCard(title = "🔓 Puertos comunes") {
        val open = r.openPorts.filter { it.open }
        if (open.isEmpty()) {
            Text("Ninguno de los puertos comunes está abierto.", style = MaterialTheme.typography.bodySmall)
        } else {
            open.forEach { p ->
                val banner = r.portBanners[p.port]
                Text("${p.port}/tcp — ${p.service}", style = MaterialTheme.typography.bodySmall)
                if (banner != null) {
                    Text("  → $banner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    InfoCard(title = "🛡️ Reputación y certificado") {
        when (r.phishingMatch) {
            true -> Text("⚠ Este dominio aparece en el feed de phishing activo de OpenPhish.", style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonRed, fontWeight = FontWeight.Bold)
            false -> Text("No aparece en el feed de phishing de OpenPhish (no garantiza que sea seguro).", style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonGreen)
            null -> {}
        }

        val certs = r.sslCerts
        if (certs.isNullOrEmpty()) {
            Text("Sin certificado SSL detectado en el puerto 443.", style = MaterialTheme.typography.bodySmall)
        } else {
            val cert = certs.first()
            InfoRow("Sujeto", cert.subject)
            InfoRow("Emisor", cert.issuer)
            InfoRow("Válido", "${cert.validFrom} → ${cert.validTo}")
            val expiryColor = when {
                cert.daysUntilExpiry < 0 -> CyberColors.NeonRed
                cert.daysUntilExpiry < 30 -> CyberColors.NeonAmber
                else -> CyberColors.NeonGreen
            }
            Text(
                if (cert.daysUntilExpiry < 0) "⚠ Certificado caducado" else "Caduca en ${cert.daysUntilExpiry} días",
                style = MaterialTheme.typography.bodySmall,
                color = expiryColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
