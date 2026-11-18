package com.example.trackrdata.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.trackrdata.data.entities.Location

data class LocationSummary (
    @Embedded
    val location: Location,

    @ColumnInfo(name = "visit_count")
    val visitCount: Int,

    @ColumnInfo(name = "last_visited_at")
    val lastVisitedAt: Long?
)