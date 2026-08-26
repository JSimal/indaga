package com.apkinves.toolbox.core.net

/**
 * Resolución IP -> ASN (a quién pertenece el bloque de direcciones a nivel de
 * enrutamiento de internet) usando el servicio gratuito de Team Cymru, que se
 * consulta como si fueran registros TXT normales de DNS — sin API, sin key,
 * reutiliza el mismo DnsClient (con su respaldo DoH incluido).
 */
object AsnLookupClient {

    data class AsnInfo(val asn: String, val name: String?, val countryCode: String?, val bgpPrefix: String?)

    suspend fun lookup(ip: String): AsnInfo? {
        val reversed = ip.trim().split(".").reversed().joinToString(".")
        val originRecords = runCatching { DnsClient.query("$reversed.origin.asn.cymru.com", DnsClient.RecordType.TXT) }
            .getOrElse { emptyList() }
        val originTxt = originRecords.firstOrNull()?.value?.trim('"') ?: return null

        // Formato: "ASN | prefijo BGP | país | registro | fecha"
        val parts = originTxt.split("|").map { it.trim() }
        val asn = parts.getOrNull(0) ?: return null
        val bgpPrefix = parts.getOrNull(1)
        val countryCode = parts.getOrNull(2)

        val nameRecords = runCatching { DnsClient.query("AS$asn.asn.cymru.com", DnsClient.RecordType.TXT) }
            .getOrElse { emptyList() }
        val nameTxt = nameRecords.firstOrNull()?.value?.trim('"')
        // Formato: "ASN | país | registro | fecha | Nombre de la organización"
        val name = nameTxt?.split("|")?.getOrNull(4)?.trim()

        return AsnInfo(asn = asn, name = name, countryCode = countryCode, bgpPrefix = bgpPrefix)
    }
}
