package com.example.frontend.api

import com.example.frontend.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface WorkflowApiService {

    // Festivals
    @GET("festivals")
    suspend fun getFestivals(): List<FestivalDto>

    // Réservations
    @GET("reservations")
    suspend fun getReservations(@Query("festivalId") festivalId: Int): List<ReservationDto>

    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<Unit>

    @PUT("reservations/{id}")
    suspend fun updateReservation(@Path("id") id: Int, @Body request: CreateReservationRequest): Response<Unit>

    @DELETE("reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>

    // Jeux festival (vue enrichie)
    @GET("jeu-festival/view")
    suspend fun getJeuFestivalView(
        @Query("festivalId") festivalId: Int,
        @Query("reservationId") reservationId: Int? = null
    ): List<JeuFestivalViewDto>

    @POST("jeu-festival")
    suspend fun addJeuFestival(@Body request: AddJeuFestivalRequest): Response<Unit>

    @DELETE("jeu-festival/{id}")
    suspend fun deleteJeuFestival(@Path("id") id: Int): Response<Unit>

    // Zones tarifaires
    @GET("zones-tarifaires")
    suspend fun getZonesTarifaires(@Query("festivalId") festivalId: Int): List<ZoneTarifaireDto>

    @POST("zones-tarifaires")
    suspend fun createZoneTarifaire(@Body request: CreateZoneTarifaireRequest): Response<Unit>

    @PUT("zones-tarifaires/{id}")
    suspend fun updateZoneTarifaire(
        @Path("id") id: Int,
        @Body request: CreateZoneTarifaireRequest
    ): Response<Unit>

    @DELETE("zones-tarifaires/{id}")
    suspend fun deleteZoneTarifaire(@Path("id") id: Int): Response<Unit>

    // Zones du plan
    @GET("zones-du-plan")
    suspend fun getZonesDuPlan(@Query("festivalId") festivalId: Int): List<ZoneDuPlanDto>

    @POST("zones-du-plan")
    suspend fun createZoneDuPlan(@Body request: CreateZoneDuPlanRequest): Response<Unit>

    @PUT("zones-du-plan/{id}")
    suspend fun updateZoneDuPlan(
        @Path("id") id: Int,
        @Body request: CreateZoneDuPlanRequest
    ): Response<Unit>

    @DELETE("zones-du-plan/{id}")
    suspend fun deleteZoneDuPlan(@Path("id") id: Int): Response<Unit>

    // Tables
    @GET("tables")
    suspend fun getTables(@Query("zoneDuPlanId") zoneDuPlanId: Int): List<TableJeuDto>

    @GET("tables/{id}/jeux")
    suspend fun getJeuxByTable(@Path("id") id: Int): List<JeuTableDto>

    @POST("tables")
    suspend fun createTable(@Body request: CreateTableRequest): Response<Unit>

    @DELETE("tables/{id}")
    suspend fun deleteTable(@Path("id") id: Int): Response<Unit>

    // Assignation jeu ↔ table
    @POST("jeu-festival-tables")
    suspend fun assignJeuToTable(@Body request: JeuFestivalTableRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "jeu-festival-tables", hasBody = true)
    suspend fun removeJeuFromTable(@Body request: JeuFestivalTableRequest): Response<Unit>

    @GET("tables/{id}")
    suspend fun getTableById(@Path("id") id: Int): TableJeuDto

    // Tables dans une réservation
    @GET("reservation-tables")
    suspend fun getReservationTables(@Query("reservationId") reservationId: Int): List<ReservationTableEntryDto>

    @POST("reservation-tables")
    suspend fun addTableToReservation(@Body request: ReservationTableRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "reservation-tables", hasBody = true)
    suspend fun removeTableFromReservation(@Body request: ReservationTableRequest): Response<Unit>
}
