package com.example.trackrdata.utils

import com.example.trackrdata.data.models.LocationSummary
import com.example.trackrdata.data.models.NearbySuggestion

fun buildNearbySuggestions(
    summaries: List<LocationSummary>,
    minimumVisits: Int = 2,
    maximumDistanceMeters: Double = 1_000.0
): List<NearbySuggestion> {
    return emptyList()
}