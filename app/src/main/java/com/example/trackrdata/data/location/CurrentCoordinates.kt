package com.example.trackrdata.data.location

data class CurrentCoordinates (
    val longitude: Double,
    val latitude: Double,
    val accuracyMeters: Float,
    val measuredAt: Long
)