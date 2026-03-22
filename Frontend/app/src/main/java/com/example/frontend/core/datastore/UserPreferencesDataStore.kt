package com.example.frontend.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        // ── Session utilisateur ────────────────────────────
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_STATUT = stringPreferencesKey("statut")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

        // ── Cookies ────────────────────────────────────────
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_ACCESS_TOKEN_EXPIRES = longPreferencesKey("access_token_expires")
        val KEY_REFRESH_TOKEN_EXPIRES = longPreferencesKey("refresh_token_expires")
    }

    // ── Lire la session ────────────────────────────────────
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { prefs ->
            UserPreferences(
                email = prefs[KEY_EMAIL] ?: "",
                role = prefs[KEY_ROLE] ?: "",
                statut = prefs[KEY_STATUT] ?: "",
                isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
            )
        }

    // ── Sauvegarder la session ─────────────────────────────
    suspend fun saveUser(email: String, role: String, statut: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_STATUT] = statut
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    // ── Sauvegarder les cookies ────────────────────────────
    suspend fun saveCookie(name: String, value: String, expiresAt: Long) {
        context.dataStore.edit { prefs ->
            when (name) {
                "access_token" -> {
                    prefs[KEY_ACCESS_TOKEN] = value
                    prefs[KEY_ACCESS_TOKEN_EXPIRES] = expiresAt
                }
                "refresh_token" -> {
                    prefs[KEY_REFRESH_TOKEN] = value
                    prefs[KEY_REFRESH_TOKEN_EXPIRES] = expiresAt
                }
            }
        }
    }

    // ── Lire les cookies (une seule fois, pas un Flow) ─────
    suspend fun getSavedCookies(): List<Triple<String, String, Long>> {
        val prefs = context.dataStore.data.first()
        val result = mutableListOf<Triple<String, String, Long>>()

        val accessToken = prefs[KEY_ACCESS_TOKEN]
        val accessExpires = prefs[KEY_ACCESS_TOKEN_EXPIRES] ?: 0L
        if (!accessToken.isNullOrEmpty()) {
            result.add(Triple("access_token", accessToken, accessExpires))
        }

        val refreshToken = prefs[KEY_REFRESH_TOKEN]
        val refreshExpires = prefs[KEY_REFRESH_TOKEN_EXPIRES] ?: 0L
        if (!refreshToken.isNullOrEmpty()) {
            result.add(Triple("refresh_token", refreshToken, refreshExpires))
        }

        return result
    }

    // ── Tout effacer (logout) ──────────────────────────────
    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = ""
            prefs[KEY_ROLE] = ""
            prefs[KEY_STATUT] = ""
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_ACCESS_TOKEN] = ""
            prefs[KEY_REFRESH_TOKEN] = ""
            prefs[KEY_ACCESS_TOKEN_EXPIRES] = 0L
            prefs[KEY_REFRESH_TOKEN_EXPIRES] = 0L
        }
    }
}