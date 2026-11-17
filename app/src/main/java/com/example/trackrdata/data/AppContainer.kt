package com.example.trackrdata.data

import android.content.Context
import com.example.trackrdata.data.location.CurrentLocationProvider
import com.example.trackrdata.data.repositories.OfflineTrackingRepository
import com.example.trackrdata.data.repositories.TrackingRepository

interface AppContainer {
    val trackingRepository : TrackingRepository
    val currentLocationProvider : CurrentLocationProvider
}

class AppDataContainer(
    private val context: Context
) : AppContainer {
    override val trackingRepository : TrackingRepository by lazy {
        OfflineTrackingRepository(
            AppDatabase.getDatabase(context)
        )
    }

    override val currentLocationProvider : CurrentLocationProvider by lazy {
        CurrentLocationProvider(context.applicationContext)
    }
}