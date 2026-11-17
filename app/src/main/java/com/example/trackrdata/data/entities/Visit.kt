package com.example.trackrdata.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["uid"],
            childColumns = ["location_uid"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("location_uid")
    ]
)
data class Visit (
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    @ColumnInfo(name = "location_uid")
    val locationUid: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "arrived_at")
    val arrivedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "departed_at")
    val departedAt: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "manual_entry")
    val manualEntry: Boolean = false,

    @ColumnInfo(name = "accuracy_meters")
    val accuracyMeters: Double? = null,
)