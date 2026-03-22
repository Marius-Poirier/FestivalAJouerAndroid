package com.example.frontend.api

import com.example.frontend.data.dto.MecanismeDto
import com.example.frontend.data.dto.TypeJeuDto
import retrofit2.http.GET

interface MetadataApiService {

    @GET("metadata/types-jeu")
    suspend fun getTypesJeu(): List<TypeJeuDto>

    @GET("metadata/mecanismes")
    suspend fun getMecanismes(): List<MecanismeDto>
}