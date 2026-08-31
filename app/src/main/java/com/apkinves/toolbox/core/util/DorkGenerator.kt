package com.apkinves.toolbox.core.util

/** Genera consultas de reconocimiento pasivo ("Google dorks") para un dominio. Sin API, solo plantillas de búsqueda. */
object DorkGenerator {

    data class Dork(val label: String, val query: String)

    fun forDomain(domain: String): List<Dork> {
        val d = domain.trim()
        return listOf(
            Dork("Todo lo indexado del dominio", "site:$d"),
            Dork("Documentos PDF", "site:$d filetype:pdf"),
            Dork("Hojas de cálculo", "site:$d (filetype:xls OR filetype:xlsx OR filetype:csv)"),
            Dork("Documentos Office", "site:$d (filetype:doc OR filetype:docx OR filetype:ppt OR filetype:pptx)"),
            Dork("Listados de directorio abiertos", "site:$d intitle:\"index of\""),
            Dork("Paneles de administración", "site:$d inurl:admin"),
            Dork("Páginas de login", "site:$d inurl:login"),
            Dork("Backups y volcados de base de datos", "site:$d (ext:sql OR ext:bak OR ext:backup)"),
            Dork("Ficheros de configuración/entorno", "site:$d (ext:env OR ext:cfg OR ext:conf OR ext:ini)"),
            Dork("Endpoints de API expuestos", "site:$d (inurl:api OR inurl:v1 OR inurl:v2)"),
            Dork("Subdominios indexados", "site:$d -www"),
            Dork("Información sensible mencionada", "site:$d (\"contraseña\" OR \"password\" OR \"confidential\")"),
            Dork("Rutas de WordPress", "site:$d inurl:wp-content"),
            Dork("Errores/trazas expuestas", "site:$d (\"stack trace\" OR \"warning:\" OR \"fatal error\")"),
        )
    }

    fun googleUrl(query: String) = "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
    fun bingUrl(query: String) = "https://www.bing.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
}
