package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.FestivalApiService
import com.example.frontend_mobile_etape1.data.dto.CreateFestivalRequest
import com.example.frontend_mobile_etape1.data.dto.FestivalDto

class FestivalRepository(private val api: FestivalApiService) {

    suspend fun getAll(): List<FestivalDto> = api.getFestivals()

    suspend fun getById(id: Int): FestivalDto = api.getFestival(id)

    suspend fun create(nom: String, lieu: String, dateDebut: String, dateFin: String) =
        api.createFestival(CreateFestivalRequest(nom, lieu, dateDebut, dateFin))

    suspend fun update(id: Int, nom: String, lieu: String, dateDebut: String, dateFin: String) =
        api.updateFestival(id, CreateFestivalRequest(nom, lieu, dateDebut, dateFin))

    suspend fun delete(id: Int) = api.deleteFestival(id)
}
