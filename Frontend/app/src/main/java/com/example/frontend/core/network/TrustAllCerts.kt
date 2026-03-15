package com.example.frontend.core.network

import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * TrustManager qui accepte tous les certificats SSL.
 * UNIQUEMENT pour le mode DEBUG avec le certificat auto-signé mkcert local.
 * Ne jamais utiliser en production.
 */
@SuppressLint("CustomX509TrustManager")
object TrustAllCerts {

    @SuppressLint("TrustAllX509TrustManager")
    private val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    fun create(): Pair<SSLContext, X509TrustManager> {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }
        return Pair(sslContext, trustManager)
    }
}
