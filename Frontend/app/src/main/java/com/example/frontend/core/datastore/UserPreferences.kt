package com.example.frontend.core.datastore

data class UserPreferences(
    val email: String = "",
    val role: String = "",
    val statut: String = "",
    val isLoggedIn: Boolean = false
)