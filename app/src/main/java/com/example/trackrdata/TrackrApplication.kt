package com.example.trackrdata

import android.app.Application
import com.example.trackrdata.data.AppContainer
import com.example.trackrdata.data.AppDataContainer

class TrackrApplication : Application() {
    lateinit var container : AppContainer private set

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}