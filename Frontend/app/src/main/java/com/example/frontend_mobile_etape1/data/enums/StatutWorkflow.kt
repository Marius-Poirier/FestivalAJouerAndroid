package com.example.frontend_mobile_etape1.data.enums

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatutWorkflow {
    @SerialName("pas_contacte") PAS_CONTACTE,
    @SerialName("contact_pris") CONTACT_PRIS,
    @SerialName("discussion_en_cours") DISCUSSION_EN_COURS,
    @SerialName("sera_absent") SERA_ABSENT,
    @SerialName("considere_absent") CONSIDERE_ABSENT,
    @SerialName("present") PRESENT,
    @SerialName("facture") FACTURE,
    @SerialName("paiement_recu") PAIEMENT_RECU,
    @SerialName("paiement_en_retard") PAIEMENT_EN_RETARD;

    /** Libellé affiché à l'utilisateur */
    val label: String get() = when (this) {
        PAS_CONTACTE -> "Pas contacté"
        CONTACT_PRIS -> "Contact pris"
        DISCUSSION_EN_COURS -> "Discussion en cours"
        SERA_ABSENT -> "Sera absent"
        CONSIDERE_ABSENT -> "Considéré absent"
        PRESENT -> "Présent"
        FACTURE -> "Facturé"
        PAIEMENT_RECU -> "Paiement reçu"
        PAIEMENT_EN_RETARD -> "Paiement en retard"
    }

    /** Valeur envoyée au backend (lowercase) */
    val apiValue: String get() = when (this) {
        PAS_CONTACTE -> "pas_contacte"
        CONTACT_PRIS -> "contact_pris"
        DISCUSSION_EN_COURS -> "discussion_en_cours"
        SERA_ABSENT -> "sera_absent"
        CONSIDERE_ABSENT -> "considere_absent"
        PRESENT -> "present"
        FACTURE -> "facture"
        PAIEMENT_RECU -> "paiement_recu"
        PAIEMENT_EN_RETARD -> "paiement_en_retard"
    }

    val badgeBackground: Color get() = when (this) {
        PAS_CONTACTE -> Color(0xFFF3F4F6)
        CONTACT_PRIS -> Color(0xFFFEF3C7)
        DISCUSSION_EN_COURS -> Color(0xFFDBEAFE)
        SERA_ABSENT -> Color(0xFFFEE2E2)
        CONSIDERE_ABSENT -> Color(0xFFFEE2E2)
        PRESENT -> Color(0xFFD1FAE5)
        FACTURE -> Color(0xFFEDE9FE)
        PAIEMENT_RECU -> Color(0xFFCCFBF1)
        PAIEMENT_EN_RETARD -> Color(0xFFFFEDD5)
    }

    val badgeText: Color get() = when (this) {
        PAS_CONTACTE -> Color(0xFF6B7280)
        CONTACT_PRIS -> Color(0xFFB45309)
        DISCUSSION_EN_COURS -> Color(0xFF1D4ED8)
        SERA_ABSENT -> Color(0xFFEF4444)
        CONSIDERE_ABSENT -> Color(0xFFB91C1C)
        PRESENT -> Color(0xFF065F46)
        FACTURE -> Color(0xFF6D28D9)
        PAIEMENT_RECU -> Color(0xFF0D9488)
        PAIEMENT_EN_RETARD -> Color(0xFFC2410C)
    }

    val dotColor: Color get() = when (this) {
        PAS_CONTACTE -> Color(0xFF888888)
        CONTACT_PRIS -> Color(0xFFF59E0B)
        DISCUSSION_EN_COURS -> Color(0xFF3B82F6)
        SERA_ABSENT -> Color(0xFFEF4444)
        CONSIDERE_ABSENT -> Color(0xFFB91C1C)
        PRESENT -> Color(0xFF10B981)
        FACTURE -> Color(0xFF8B5CF6)
        PAIEMENT_RECU -> Color(0xFF0D9488)
        PAIEMENT_EN_RETARD -> Color(0xFFF97316)
    }
}
