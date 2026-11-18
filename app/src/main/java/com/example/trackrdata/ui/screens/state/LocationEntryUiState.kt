package com.example.trackrdata.ui.screens.state

import com.example.trackrdata.data.location.CurrentCoordinates

data class LocationEntryUiState(
    val coordinates: CurrentCoordinates? = null,
    val isCapturing: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)
