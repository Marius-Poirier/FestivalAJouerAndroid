package com.example.frontend

import android.app.Application
import com.example.frontend.core.network.RetrofitInstance
import com.example.frontend.data.database.FestivalDatabase
import com.example.frontend.data.repository.OfflineWorkflowRepository

class FestivalApp : Application() {
    val database: FestivalDatabase by lazy {
        FestivalDatabase.getDatabase(this)
    }
    val offlineRepository: OfflineWorkflowRepository by lazy {
        OfflineWorkflowRepository(
            festivalDao       = database.festivalDao(),
            editeurDao        = database.editeurDao(),
            reservationDao    = database.reservationDao(),
            jeuFestivalDao    = database.jeuFestivalDao(),
            zoneTarifaireDao  = database.zoneTarifaireDao(),
            zoneDuPlanDao     = database.zoneDuPlanDao(),
            tableJeuDao       = database.tableJeuDao(),
            jeuTableDao       = database.jeuTableDao(),
            reservationTableDao = database.reservationTableDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.init(this)
    }
}