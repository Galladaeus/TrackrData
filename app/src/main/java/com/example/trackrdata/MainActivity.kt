package com.example.trackrdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.example.trackrdata.ui.theme.TrackrDataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ArcGISEnvironment.apiKey = ApiKey.create(BuildConfig.ARCGIS_API_KEY)

        enableEdgeToEdge()

        setContent {
            TrackrDataTheme() {
                TrackrApp()
            }
        }
    }
}
