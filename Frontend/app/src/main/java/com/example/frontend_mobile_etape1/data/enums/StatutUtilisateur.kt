package com.example.frontend_mobile_etape1.data.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatutUtilisateur {
    @SerialName("en_attente") EN_ATTENTE,
    @SerialName("valide") VALIDE,
    @SerialName("refuse") REFUSE
}
