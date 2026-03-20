package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PlanApiService {

    // ────────────────────────────────────────────────
    // ZONES TARIFAIRES
    // ────────────────────────────────────────────────

    @GET("zones-tarifaires")
    suspend fun getZonesTarifaires(
        @Query("festivalId") festivalId: Int? = null
    ): List<ZoneTarifaireDto>

    @GET("zones-tarifaires/{id}")
    suspend fun getZoneTarifaire(@Path("id") id: Int): ZoneTarifaireDto

    @POST("zones-tarifaires")
    suspend fun createZoneTarifaire(@Body request: CreateZoneTarifaireRequest): Response<Map<String, Any>>

    @PUT("zones-tarifaires/{id}")
    suspend fun updateZoneTarifaire(
        @Path("id") id: Int,
        @Body request: CreateZoneTarifaireRequest
    ): Response<Map<String, Any>>

    @DELETE("zones-tarifaires/{id}")
    suspend fun deleteZoneTarifaire(@Path("id") id: Int): Response<Unit>

    // ────────────────────────────────────────────────
    // ZONES DU PLAN
    // ────────────────────────────────────────────────

    @GET("zones-du-plan")
    suspend fun getZonesDuPlan(
        @Query("festivalId") festivalId: Int? = null
    ): List<ZoneDuPlanDto>

    @POST("zones-du-plan")
    suspend fun createZoneDuPlan(@Body request: CreateZoneDuPlanRequest): Response<Map<String, Any>>

    @PUT("zones-du-plan/{id}")
    suspend fun updateZoneDuPlan(
        @Path("id") id: Int,
        @Body request: CreateZoneDuPlanRequest
    ): Response<Map<String, Any>>

    @DELETE("zones-du-plan/{id}")
    suspend fun deleteZoneDuPlan(@Path("id") id: Int): Response<Unit>

    // ────────────────────────────────────────────────
    // TABLES
    // ────────────────────────────────────────────────

    @GET("tables")
    suspend fun getTables(
        @Query("zoneDuPlanId") zoneDuPlanId: Int? = null
    ): List<TableJeuDto>

    @GET("tables/{id}")
    suspend fun getTable(@Path("id") id: Int): TableJeuDto

    @GET("tables/{id}/jeux")
    suspend fun getTableJeux(@Path("id") id: Int): List<JeuTableDto>

    @POST("tables")
    suspend fun createTable(@Body request: CreateTableRequest): Response<Map<String, Any>>

    @DELETE("tables/{id}")
    suspend fun deleteTable(@Path("id") id: Int): Response<Unit>
}
