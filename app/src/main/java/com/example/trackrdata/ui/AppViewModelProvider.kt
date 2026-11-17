package com.example.trackrdata.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.trackrdata.TrackrApplication
import com.example.trackrdata.ui.viewModels.TrackingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application =
                this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as TrackrApplication

            TrackingViewModel(
                application.container.trackingRepository,
                application.container.currentLocationProvider
            )
        }
    }
}