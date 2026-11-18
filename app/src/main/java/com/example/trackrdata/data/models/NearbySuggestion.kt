package com.example.trackrdata.data.models

data class NearbySuggestion(
    val first: LocationSummary,
    val second: LocationSummary,
    val distanceMeters: Double
)
