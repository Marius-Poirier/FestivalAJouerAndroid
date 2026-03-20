package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.ReservationApiService
import com.example.frontend_mobile_etape1.data.dto.CreateReservationRequest
import com.example.frontend_mobile_etape1.data.dto.ReservationDto
import com.example.frontend_mobile_etape1.data.dto.UpdateReservationRequest

class ReservationRepository (private val api: ReservationApiService) {

    suspend fun getAll(
        festivalId: Int? = null,
        editeurId: Int? = null,
        statut: String? = null
    ): List<ReservationDto> = api.getReservations(festivalId = festivalId, editeurId = editeurId, statut = statut)

    suspend fun getById(id: Int): ReservationDto = api.getReservation(id)

    suspend fun create(request: CreateReservationRequest) = api.createReservation(request)

    suspend fun update(id: Int, request: UpdateReservationRequest) =
        api.updateReservation(id, request)

    suspend fun delete(id: Int) = api.deleteReservation(id)
}
