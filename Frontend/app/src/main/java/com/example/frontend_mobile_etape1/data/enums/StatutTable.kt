package com.example.frontend_mobile_etape1.data.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatutTable {
    @SerialName("libre") LIBRE,
    @SerialName("reserve") RESERVE,
    @SerialName("plein") PLEIN,
    @SerialName("hors_service") HORS_SERVICE;

    val label: String get() = when (this) {
        LIBRE -> "Libre"
        RESERVE -> "Réservée"
        PLEIN -> "Pleine"
        HORS_SERVICE -> "Hors service"
    }
}
