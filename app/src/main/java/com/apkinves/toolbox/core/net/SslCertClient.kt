package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import javax.net.ssl.SSLSocketFactory

object SslCertClient {

    data class CertInfo(
        val subject: String,
        val issuer: String,
        val validFrom: String,
        val validTo: String,
        val daysUntilExpiry: Long,
        val signatureAlgorithm: String,
    )

    suspend fun inspect(host: String, port: Int = 443): Result<List<CertInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            factory.createSocket(host, port).use { socket ->
                val sslSocket = socket as javax.net.ssl.SSLSocket
                sslSocket.soTimeout = 6000
                sslSocket.startHandshake()
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sslSocket.session.peerCertificates
                    .filterIsInstance<X509Certificate>()
                    .map { cert ->
                        val now = System.currentTimeMillis()
                        val daysLeft = (cert.notAfter.time - now) / (1000 * 60 * 60 * 24)
                        CertInfo(
                            subject = cert.subjectX500Principal.name,
                            issuer = cert.issuerX500Principal.name,
                            validFrom = format.format(cert.notBefore),
                            validTo = format.format(cert.notAfter),
                            daysUntilExpiry = daysLeft,
                            signatureAlgorithm = cert.sigAlgName,
                        )
                    }
            }
        }
    }
}
