package com.example.frontend_mobile_etape1.data.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RoleUtilisateur {
    @SerialName("admin") ADMIN,
    @SerialName("super_organisateur") SUPER_ORGANISATEUR,
    @SerialName("organisateur") ORGANISATEUR,
    @SerialName("benevole") BENEVOLE;

    companion object {
        fun fromString(value: String?): RoleUtilisateur? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.replace("_", "").equals(value?.replace("_", ""), ignoreCase = true) }
    }
}
