package com.example.trackrdata

import android.app.Application
import android.util.Log
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.LicenseKey
import com.arcgismaps.exceptions.LicensingException
import com.example.trackrdata.data.AppContainer
import com.example.trackrdata.data.AppDataContainer

class TrackrApplication : Application() {
    lateinit var container : AppContainer private set

    override fun onCreate() {
        super.onCreate()

        val licenseKey = LicenseKey.create(BuildConfig.ARCGIS_LICENSE_KEY)

        if (licenseKey == null) {
            Log.e("TrackrData", "Invalid ArcGIS license string.")
        } else {
            try {
                val result = ArcGISEnvironment.setLicense(licenseKey)

                Log.i(
                    "TrackrData",
                    "ArcGIS license status: ${result?.licenseStatus}"
                )
            } catch (e: LicensingException) {
                Log.e("TrackrData", "ArcGIS licensing failed.", e)
            }
        }

        ArcGISEnvironment.apiKey = ApiKey.create(BuildConfig.ARCGIS_API_KEY)

        container = AppDataContainer(this)
    }
}