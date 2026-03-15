package com.example.frontend.core.network

import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator OkHttp : sur réception d'un 401, tente automatiquement
 * un refresh token (POST /auth/refresh) puis rejoue la requête originale.
 * Miroir exact du comportement de l'interceptor Angular.
 */
class AuthAuthenticator(
    private val cookieJar: AppCookieJar
) : Authenticator {

    // Évite les boucles infinies : max 1 retry par requête
    override fun authenticate(route: Route?, response: Response): Request? {
        // Si on a déjà retenté, on abandonne
        if (response.request.header("X-Auth-Retry") != null) return null

        // Ne pas intercepter les routes d'auth elles-mêmes
        val url = response.request.url.toString()
        val excluded = listOf("/auth/login", "/auth/logout", "/auth/refresh", "/auth/whoami")
        if (excluded.any { url.contains(it) }) return null

        // Tente le refresh
        return try {
            val refreshClient = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()

            val refreshRequest = Request.Builder()
                .url("https://api.mxrjup.fun/api/auth/refresh")
                .post("{}".toRequestBody())
                .build()

            val refreshResponse = refreshClient.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                // Rejoue la requête originale avec le nouveau cookie
                response.request.newBuilder()
                    .header("X-Auth-Retry", "true")
                    .build()
            } else {
                cookieJar.clearAll()
                null
            }
        } catch (e: Exception) {
            cookieJar.clearAll()
            null
        }
    }
}
