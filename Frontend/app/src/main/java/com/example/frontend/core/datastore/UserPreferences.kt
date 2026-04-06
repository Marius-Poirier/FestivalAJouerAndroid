package com.example.frontend.core.datastore

//Class pour stocker les information qu'on souhaite sauvegarder sur notre mobile
//ici c'est la session de l'utilisateur evite de devoir se reconncter a chaque fois qu'il ferme l'application
data class UserPreferences(
    val email: String = "",
    val role: String = "",
    val statut: String = "",
    val isLoggedIn: Boolean = false
)