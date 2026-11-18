package com.example.trackrdata.ui.screens.state

import com.example.trackrdata.data.location.CurrentCoordinates

data class TrackingUiState(
    val isTracking: Boolean = false,
    val sessionId: Long? = null,
    val samplesRecorded: Int = 0,
    val lastCoordinates: CurrentCoordinates? = null,
    val message: String? = null
)
