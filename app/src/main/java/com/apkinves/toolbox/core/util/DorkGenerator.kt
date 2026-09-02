package com.apkinves.toolbox.core.util

/** Genera consultas de reconocimiento pasivo ("Google dorks") para un dominio. Sin API, solo plantillas de búsqueda. */
object DorkGenerator {

    enum class Risk { ALTO, MEDIO, BAJO }

    data class Dork(val label: String, val risk: Risk, val query: String)

    fun forDomain(domain: String): List<Dork> {
        val d = domain.trim()
        return listOf(
            // --- Alto interés: fugas de datos/credenciales reales ---
            Dork("Repositorio .git expuesto (código fuente completo)", Risk.ALTO, "site:$d inurl:\".git\""),
            Dork("Backups y volcados de base de datos", Risk.ALTO, "site:$d (ext:sql OR ext:bak OR ext:backup OR ext:dump)"),
            Dork("Ficheros de configuración/entorno", Risk.ALTO, "site:$d (ext:env OR ext:cfg OR ext:conf OR ext:ini OR ext:yml)"),
            Dork("Claves y credenciales mencionadas", Risk.ALTO, "site:$d (intext:\"api_key\" OR intext:\"secret_key\" OR intext:\"password\" OR intext:\"BEGIN PRIVATE KEY\")"),
            Dork("Menciones en Pastebin (credenciales filtradas)", Risk.ALTO, "site:pastebin.com \"$d\""),
            Dork("Buckets S3 mal configurados", Risk.ALTO, "site:s3.amazonaws.com \"$d\""),
            Dork("Listados de directorio abiertos", Risk.ALTO, "site:$d intitle:\"index of\""),
            Dork("phpinfo() expuesto (config. completa del servidor)", Risk.ALTO, "site:$d inurl:phpinfo.php"),

            // --- Interés medio: superficie de ataque / documentos ---
            Dork("Paneles de administración", Risk.MEDIO, "site:$d inurl:admin"),
            Dork("Páginas de login", Risk.MEDIO, "site:$d inurl:login"),
            Dork("Endpoints de API / documentación", Risk.MEDIO, "site:$d (inurl:api OR inurl:swagger OR inurl:api-docs)"),
            Dork("Paneles CI/CD (Jenkins, GitLab...)", Risk.MEDIO, "site:$d (inurl:jenkins OR inurl:\"gitlab/-/settings\")"),
            Dork("Restablecer contraseña", Risk.MEDIO, "site:$d inurl:\"reset-password\""),
            Dork("Hojas de cálculo (posibles emails/datos internos)", Risk.MEDIO, "site:$d (filetype:xls OR filetype:xlsx OR filetype:csv)"),
            Dork("Documentos PDF", Risk.MEDIO, "site:$d filetype:pdf"),
            Dork("Documentos Office", Risk.MEDIO, "site:$d (filetype:doc OR filetype:docx OR filetype:ppt OR filetype:pptx)"),
            Dork("Subdominios indexados", Risk.MEDIO, "site:$d -www"),
            Dork("Errores/trazas expuestas", Risk.MEDIO, "site:$d (\"stack trace\" OR \"warning:\" OR \"fatal error\")"),

            // --- Interés bajo: reconocimiento general ---
            Dork("Rutas de WordPress", Risk.BAJO, "site:$d inurl:wp-content"),
            Dork("Información sensible mencionada", Risk.BAJO, "site:$d (\"contraseña\" OR \"confidential\")"),
            Dork("Todo lo indexado del dominio", Risk.BAJO, "site:$d"),
        )
    }

    fun googleUrl(query: String) = "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
    fun bingUrl(query: String) = "https://www.bing.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
}
