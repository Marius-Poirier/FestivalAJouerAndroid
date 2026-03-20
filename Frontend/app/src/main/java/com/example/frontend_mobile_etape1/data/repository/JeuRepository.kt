package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.JeuApiService
import com.example.frontend_mobile_etape1.api.MetadataApiService
import com.example.frontend_mobile_etape1.data.dto.CreateJeuRequest
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.dto.MecanismeDto
import com.example.frontend_mobile_etape1.data.dto.TypeJeuDto

class JeuRepository(
    private val jeuApi: JeuApiService,
    private val metadataApi: MetadataApiService
) {

    suspend fun getAll(search: String? = null, sortBy: String? = null): List<JeuDto> =
        jeuApi.getJeux(search = search, sortBy = sortBy)

    suspend fun getById(id: Int): JeuDto = jeuApi.getJeu(id)

    suspend fun create(request: CreateJeuRequest) = jeuApi.createJeu(request)

    suspend fun update(id: Int, request: CreateJeuRequest) = jeuApi.updateJeu(id, request)

    suspend fun delete(id: Int) = jeuApi.deleteJeu(id)

    suspend fun getTypesJeu(): List<TypeJeuDto> = metadataApi.getTypesJeu()

    suspend fun getMecanismes(): List<MecanismeDto> = metadataApi.getMecanismes()
}
