package com.apkinves.toolbox.core.util

import java.net.URLEncoder

/** Servicios de verificación de fraude/scam sin API gratuita: se abren en el navegador con el dominio ya puesto. */
object ScamCheckLinks {
    data class Site(val label: String, val urlTemplate: String, val directLookup: Boolean)

    val SITES = listOf(
        Site("ScamAdviser", "https://www.scamadviser.com/check-website/%s", directLookup = true),
        Site("ScamCharge", "https://scamcharge.com/?s=%s", directLookup = false),
        Site("Fraud.org", "https://fraud.org/?s=%s", directLookup = false),
    )

    fun urlFor(site: Site, domain: String): String {
        val encoded = URLEncoder.encode(domain.trim(), "UTF-8")
        return site.urlTemplate.format(encoded)
    }
}
