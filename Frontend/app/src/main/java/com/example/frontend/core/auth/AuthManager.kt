package com.example.frontend.core.auth

import com.example.frontend.api.auth.AuthApiService
import com.example.frontend.core.network.AppCookieJar
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.dto.UserMeResponse
import com.example.frontend.data.enums.RoleUtilisateur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.frontend.data.dto.LoginRequest
import kotlinx.coroutines.launch

/**
 * Gestionnaire central de l'état d'authentification.
 * Miroir de l'AuthService Angular : expose l'utilisateur courant et les helpers de rôles.
 */
class AuthManager(private val apiService: AuthApiService, private val cookieJar: AppCookieJar) {
    private val _currentUser = MutableStateFlow<UserMeResponse?>(null)
    val currentUser: StateFlow<UserMeResponse?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean get() = _currentUser.value != null

    val currentRole: RoleUtilisateur?
        get() = _currentUser.value?.roleEnum
    val isAdmin: Boolean get() = currentRole == RoleUtilisateur.ADMIN
    val isSuperOrganisateur: Boolean get() = currentRole == RoleUtilisateur.SUPER_ORGANISATEUR
    val isOrganisateur: Boolean get() = currentRole == RoleUtilisateur.ORGANISATEUR
    val isBenevole: Boolean get() = currentRole == RoleUtilisateur.BENEVOLE

    /** ADMIN || SUPER_ORGANISATEUR || ORGANISATEUR */
    val isAdminSuperorgaOrga: Boolean get() = isAdmin || isSuperOrganisateur || isOrganisateur

    /** ADMIN || SUPER_ORGANISATEUR */
    val isAdminSuperorga: Boolean get() = isAdmin || isSuperOrganisateur

    /**
     * Vérifie la session auprès du backend et stocke l'utilisateur courant.
     * Appelé au démarrage de l'app et après chaque login.
     */
     suspend fun whoami(): Boolean {
        return try {
            val response = apiService.getMe()

            if (response.isSuccessful && response.body() != null) {

                _currentUser.value = response.body()
                true
            } else {
                _currentUser.value = null
                false
            }
        } catch (e: Exception) {
            _currentUser.value = null
            false
        }
    }

    fun setUser(user: UserMeResponse?) {
        _currentUser.value = user
    }

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body()?.user != null) {
                val ok = whoami()
                if (ok) {
                    val user = _currentUser.value
                    if (user != null) {
                        RetrofitInstance.userPreferencesDataStore.saveUser(
                            email = user.email,
                            role = user.role,
                            statut = user.statut ?: ""
                        )
                    }
                    LoginResult.Success
                } else {
                    LoginResult.Error("Impossible de récupérer les informations du compte")
                }
            } else {
                val message = when (response.code()) {
                    401 -> "Identifiants invalides"
                    403 -> "Compte en attente de validation ou bloqué"
                    0 -> "Serveur injoignable"
                    else -> "Erreur serveur (${response.code()})"
                }
                LoginResult.Error(message)
            }
        } catch (e: Exception) {
            LoginResult.Error("Impossible de contacter le serveur : ${e.message}")
        }
    }
//paramettre car c'est pas un vm
    fun logout(scope: kotlinx.coroutines.CoroutineScope) {
        _currentUser.value = null
        cookieJar.clearAll()
         scope.launch {
            RetrofitInstance.userPreferencesDataStore.clearUser()
        }
    }
}
