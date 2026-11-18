package com.example.trackrdata.ui.screens.state

data class TrackingUiState(
    val isTracking: Boolean = false,
    val sessionId: Long? = null,
    val samplesRecorded: Int = 0,
    val message: String? = null
)
