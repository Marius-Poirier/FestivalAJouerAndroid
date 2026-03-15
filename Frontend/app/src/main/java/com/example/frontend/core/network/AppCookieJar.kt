package com.example.frontend.core.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * CookieJar en mémoire persistant pour la session OkHttp.
 * Gère les cookies httpOnly du backend (access_token + refresh_token).
 */
class AppCookieJar : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = cookieStore[host]?.toMutableList() ?: mutableListOf()
        cookies.forEach { newCookie ->
            existing.removeAll { it.name == newCookie.name }
            existing.add(newCookie)
        }
        cookieStore[host] = existing
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host]?.filter { cookie ->
            !cookie.expiresAt.let { it > 0 && it < System.currentTimeMillis() }
        } ?: emptyList()
    }

    fun clearAll() {
        cookieStore.clear()
    }

    fun hasCookies(): Boolean = cookieStore.isNotEmpty()
}
