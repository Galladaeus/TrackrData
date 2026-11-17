package com.example.trackrdata.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class LocationWithVisits (
    @Embedded val location: Location,
    @Relation(
        parentColumn = "uid",
        entityColumn = "location_uid"
    )
    val visits: List<Visit>
)