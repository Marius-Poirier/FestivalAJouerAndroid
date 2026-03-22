package com.example.frontend

import android.app.Application
import com.example.frontend.core.network.RetrofitInstance

class FestivalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.init(this)
    }
}