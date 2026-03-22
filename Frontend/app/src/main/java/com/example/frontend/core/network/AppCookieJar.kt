package com.example.frontend.core.network

import com.example.frontend.core.datastore.UserPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class AppCookieJar(private val dataStore: UserPreferencesDataStore? = null) : CookieJar {

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── Appelé au démarrage pour recharger les cookies du disque ──
    fun restoreCookies(host: String, cookies: List<Cookie>) {
        if (cookies.isNotEmpty()) {
            cookieStore[host] = cookies
            android.util.Log.d("COOKIES", "Cookies restaurés pour $host : ${cookies.map { it.name }}")
        }
    }

    // ── OkHttp appelle ça quand le backend envoie des cookies ──
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = cookieStore[host]?.toMutableList() ?: mutableListOf()
        cookies.forEach { newCookie ->
            existing.removeAll { it.name == newCookie.name }
            existing.add(newCookie)

            // Sauvegarde sur le disque si c'est un cookie de session
            if (newCookie.name == "access_token" || newCookie.name == "refresh_token") {
                dataStore?.let {
                    scope.launch {
                        it.saveCookie(
                            name = newCookie.name,
                            value = newCookie.value,
                            expiresAt = newCookie.expiresAt
                        )
                        android.util.Log.d("COOKIES", "Cookie sauvegardé : ${newCookie.name}")
                    }
                }
            }
        }
        cookieStore[host] = existing
    }

    // ── OkHttp appelle ça avant chaque requête ──
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