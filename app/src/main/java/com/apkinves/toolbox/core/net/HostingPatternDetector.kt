package com.apkinves.toolbox.core.net

/**
 * Muchas plataformas dejan un rastro inequívoco en el CNAME de un dominio o
 * subdominio (ej. "algo.herokuapp.com"). Cuando aparece, es una señal mucho
 * más fiable que el "org" de una API de geolocalización de IP.
 */
object HostingPatternDetector {

    private val PATTERNS = listOf(
        "herokuapp.com" to "Heroku",
        "herokudns.com" to "Heroku",
        "s3.amazonaws.com" to "Amazon S3",
        "cloudfront.net" to "Amazon CloudFront",
        "elasticbeanstalk.com" to "AWS Elastic Beanstalk",
        "azurewebsites.net" to "Microsoft Azure",
        "azureedge.net" to "Microsoft Azure CDN",
        "github.io" to "GitHub Pages",
        "gitlab.io" to "GitLab Pages",
        "netlify.app" to "Netlify",
        "vercel.app" to "Vercel",
        "pantheonsite.io" to "Pantheon",
        "wixdns.net" to "Wix",
        "myshopify.com" to "Shopify",
        "squarespace.com" to "Squarespace",
        "fastly.net" to "Fastly",
        "cloudflare.net" to "Cloudflare",
        "appspot.com" to "Google App Engine",
        "ghs.google.com" to "Google Sites/Hosting",
        "digitaloceanspaces.com" to "DigitalOcean Spaces",
        "ovh.net" to "OVH",
    )

    fun detect(cnameTargets: List<String>): String? {
        for (target in cnameTargets) {
            PATTERNS.firstOrNull { (pattern, _) -> target.contains(pattern, ignoreCase = true) }?.let {
                return it.second
            }
        }
        return null
    }
}
