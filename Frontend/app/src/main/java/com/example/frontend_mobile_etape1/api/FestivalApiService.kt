package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.FestivalDto
import com.example.frontend_mobile_etape1.data.dto.CreateFestivalRequest
import retrofit2.Response
import retrofit2.http.*

interface FestivalApiService {

    @GET("festivals")
    suspend fun getFestivals(): List<FestivalDto>

    @GET("festivals/{id}")
    suspend fun getFestival(@Path("id") id: Int): FestivalDto

    @POST("festivals")
    suspend fun createFestival(@Body request: CreateFestivalRequest): Response<Map<String, Any>>

    @PUT("festivals/{id}")
    suspend fun updateFestival(
        @Path("id") id: Int,
        @Body request: CreateFestivalRequest
    ): Response<Map<String, Any>>

    @DELETE("festivals/{id}")
    suspend fun deleteFestival(@Path("id") id: Int): Response<Unit>
}
