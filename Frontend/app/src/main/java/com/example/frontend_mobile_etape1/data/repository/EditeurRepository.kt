package com.example.frontend_mobile_etape1.data.repository

import com.example.frontend_mobile_etape1.api.EditeurApiService
import com.example.frontend_mobile_etape1.data.dto.CreateEditeurRequest
import com.example.frontend_mobile_etape1.data.dto.CreatePersonneRequest
import com.example.frontend_mobile_etape1.data.dto.EditeurDto
import com.example.frontend_mobile_etape1.data.dto.JeuDto
import com.example.frontend_mobile_etape1.data.dto.PersonneDto


class EditeurRepository (private val api: EditeurApiService) {

    suspend fun getAll(search: String? = null, festivalId: Int? = null): List<EditeurDto> =
        api.getEditeurs(search = search, festivalId = festivalId)

    suspend fun getById(id: Int): EditeurDto = api.getEditeur(id)

    suspend fun create(nom: String, logoUrl: String?) =
        api.createEditeur(CreateEditeurRequest(nom, logoUrl))

    suspend fun update(id: Int, nom: String, logoUrl: String?) =
        api.updateEditeur(id, CreateEditeurRequest(nom, logoUrl))

    suspend fun delete(id: Int) = api.deleteEditeur(id)

    suspend fun getJeux(id: Int): List<JeuDto> = api.getJeuxByEditeur(id)

    suspend fun getPersonnes(id: Int): List<PersonneDto> = api.getPersonnesByEditeur(id)

    suspend fun addPersonne(editeurId: Int, request: CreatePersonneRequest) =
        api.addPersonneToEditeur(editeurId, request)

    suspend fun removePersonne(editeurId: Int, personneId: Int) =
        api.removePersonneFromEditeur(editeurId, personneId)
}
