package com.example.frontend_mobile_etape1.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formate une date ISO (2025-05-01T00:00:00.000Z ou 2025-05-01) en format français dd/MM/yyyy.
 * Retourne la valeur brute en cas d'erreur de parsing.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun formatDateFr(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val cleaned = iso.substringBefore('T')
        val date = LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE)
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        iso
    }
}
