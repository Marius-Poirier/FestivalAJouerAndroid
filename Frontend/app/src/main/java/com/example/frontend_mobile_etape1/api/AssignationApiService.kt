package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AssignationApiService {

    // ────────────────────────────────────────────────
    // JEU-FESTIVAL
    // ────────────────────────────────────────────────

    @GET("jeu-festival/view")
    suspend fun getJeuxFestival(
        @Query("festivalId") festivalId: Int? = null,
        @Query("reservationId") reservationId: Int? = null
    ): List<JeuFestivalViewDto>

    @POST("jeu-festival")
    suspend fun addJeuFestival(@Body request: AddJeuFestivalRequest): Response<Map<String, Any>>

    @DELETE("jeu-festival/{id}")
    suspend fun removeJeuFestival(@Path("id") id: Int): Response<Unit>

    // ────────────────────────────────────────────────
    // RESERVATION-TABLES (lien réservation ↔ table)
    // ────────────────────────────────────────────────

    @GET("reservation-tables")
    suspend fun getReservationTables(
        @Query("reservationId") reservationId: Int? = null
    ): List<ReservationTableDto>

    @POST("reservation-tables")
    suspend fun addTableToReservation(@Body request: ReservationTableRequest): Response<Map<String, Any>>

    @HTTP(method = "DELETE", path = "reservation-tables", hasBody = true)
    suspend fun removeTableFromReservation(@Body request: ReservationTableRequest): Response<Map<String, Any>>

    // ────────────────────────────────────────────────
    // JEU-FESTIVAL-TABLES (assignation jeux ↔ tables)
    // ────────────────────────────────────────────────

    @POST("jeu-festival-tables")
    suspend fun assignJeuToTable(@Body request: JeuFestivalTableRequest): Response<Map<String, Any>>

    @HTTP(method = "DELETE", path = "jeu-festival-tables", hasBody = true)
    suspend fun unassignJeuFromTable(@Body request: JeuFestivalTableRequest): Response<Map<String, Any>>
}
