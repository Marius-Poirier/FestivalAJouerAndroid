package com.example.frontend.api

import com.example.frontend.data.dto.EditeurDto
import com.example.frontend.data.dto.CreateEditeurRequest
import com.example.frontend.data.dto.CreatePersonneRequest
import com.example.frontend.data.dto.JeuDto
import com.example.frontend.data.dto.PersonneDto
import retrofit2.Response
import retrofit2.http.*

interface EditeurApiService {

    @GET("editeurs")
    suspend fun getEditeurs(
        @Query("search") search: String? = null,
        @Query("festivalId") festivalId: Int? = null
    ): List<EditeurDto>

    @GET("editeurs/{id}")
    suspend fun getEditeur(@Path("id") id: Int): EditeurDto

    @POST("editeurs")
    suspend fun createEditeur(@Body request: CreateEditeurRequest): Response<EditeurDto>

    @PUT("editeurs/{id}")
    suspend fun updateEditeur(
        @Path("id") id: Int,
        @Body request: CreateEditeurRequest
    ): Response<EditeurDto>

    @DELETE("editeurs/{id}")
    suspend fun deleteEditeur(@Path("id") id: Int): Response<Unit>

    @GET("editeurs/{id}/jeux")
    suspend fun getJeuxByEditeur(@Path("id") id: Int): List<JeuDto>

    @GET("editeurs/{id}/personnes")
    suspend fun getPersonnesByEditeur(@Path("id") id: Int): List<PersonneDto>

    @POST("editeurs/{id}/personnes")
    suspend fun addPersonneToEditeur(
        @Path("id") id: Int,
        @Body request: CreatePersonneRequest
    ): Response<Map<String, Any>>

    @DELETE("editeurs/{editorId}/personnes/{personneId}")
    suspend fun removePersonneFromEditeur(
        @Path("editorId") editorId: Int,
        @Path("personneId") personneId: Int
    ): Response<Unit>

}
