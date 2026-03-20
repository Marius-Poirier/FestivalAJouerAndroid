package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.ReservationDto
import com.example.frontend_mobile_etape1.data.dto.CreateReservationRequest
import com.example.frontend_mobile_etape1.data.dto.UpdateReservationRequest
import retrofit2.Response
import retrofit2.http.*

interface ReservationApiService {

    @GET("reservations")
    suspend fun getReservations(
        @Query("festivalId") festivalId: Int? = null,
        @Query("editeurId") editeurId: Int? = null,
        @Query("statut") statut: String? = null,
        @Query("search") search: String? = null
    ): List<ReservationDto>

    @GET("reservations/{id}")
    suspend fun getReservation(@Path("id") id: Int): ReservationDto

    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<Map<String, Any>>

    @PUT("reservations/{id}")
    suspend fun updateReservation(
        @Path("id") id: Int,
        @Body request: UpdateReservationRequest
    ): Response<Map<String, Any>>

    @DELETE("reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>
}
