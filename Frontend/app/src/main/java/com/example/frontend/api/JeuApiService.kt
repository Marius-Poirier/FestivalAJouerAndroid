package com.example.frontend.api

import com.example.frontend.data.dto.JeuDto
import com.example.frontend.data.dto.CreateJeuRequest
import retrofit2.Response
import retrofit2.http.*

interface JeuApiService {

    @GET("jeux")
    suspend fun getJeux(
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null
    ): List<JeuDto>

    @GET("jeux/{id}")
    suspend fun getJeu(@Path("id") id: Int): JeuDto

    @POST("jeux")
    suspend fun createJeu(@Body request: CreateJeuRequest): Response<Unit>

    @PUT("jeux/{id}")
    suspend fun updateJeu(
        @Path("id") id: Int,
        @Body request: CreateJeuRequest
    ): Response<Unit>

    @DELETE("jeux/{id}")
    suspend fun deleteJeu(@Path("id") id: Int): Response<Unit>
}
