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

    // ── Appelé au démarrage pour recharger les cookies du disque (dataStore)
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

    // ── filtrer pour garder que les token qui s'en sont pas expiré
    // OkHttp appelle ça quand il envoie une requête au backend automatiquement
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host]?.filter { cookie ->
            !cookie.expiresAt.let { it > 0 && it < System.currentTimeMillis() } //on garde le token si pas de date d'expiration ou si la date est inferieur a la date actuelle
        } ?: emptyList() // si pas de token on renvoie une liste vide
    }

    fun clearAll() {
        cookieStore.clear()
    }

    fun hasCookies(): Boolean = cookieStore.isNotEmpty()
}