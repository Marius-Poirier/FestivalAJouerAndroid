package com.example.frontend_mobile_etape1.api

import com.example.frontend_mobile_etape1.data.dto.TypeJeuDto
import com.example.frontend_mobile_etape1.data.dto.MecanismeDto
import retrofit2.http.*

interface MetadataApiService {

    @GET("metadata/types-jeu")
    suspend fun getTypesJeu(): List<TypeJeuDto>

    @GET("metadata/mecanismes")
    suspend fun getMecanismes(): List<MecanismeDto>
}
