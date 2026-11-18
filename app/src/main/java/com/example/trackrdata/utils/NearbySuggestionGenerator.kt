package com.example.trackrdata.utils

import com.example.trackrdata.data.models.LocationSummary
import com.example.trackrdata.data.models.NearbySuggestion
import android.location.Location as DeviceLocation

fun buildNearbySuggestions(
    summaries: List<LocationSummary>,
    minimumVisits: Int = 2,
    maximumDistanceMeters: Double = 1_000.0
): List<NearbySuggestion> {
    val eligible = summaries
        .filter {
            val location = it.location

            it.visitCount >= minimumVisits &&
                location.latitude.isFinite() &&
                location.longitude.isFinite() &&
                location.latitude in -90.0..90.0 &&
                location.longitude in -180.0..180.0
        }
        .sortedBy { it.location.uid }

    val suggestions = mutableListOf<NearbySuggestion>()

    for (firstIndex in eligible.indices) {
        for (secondIndex in firstIndex + 1 until eligible.size) {
            val first = eligible[firstIndex]
            val second = eligible[secondIndex]

            val result = FloatArray(1)

            DeviceLocation.distanceBetween(
                first.location.latitude,
                first.location.longitude,
                second.location.latitude,
                second.location.longitude,
                result
            )

            val distance = result[0].toDouble()

            if (distance in 50.0..maximumDistanceMeters) {
                suggestions.add(
                    NearbySuggestion(
                        first = first,
                        second = second,
                        distanceMeters = distance
                    )
                )
            }
        }
    }

    return suggestions
        .sortedBy { it.distanceMeters }
        .take(3)
}