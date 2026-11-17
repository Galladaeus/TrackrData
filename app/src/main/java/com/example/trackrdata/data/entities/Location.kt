package com.example.trackrdata.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location (
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "notes")
    val notes: String,

    @ColumnInfo(name = "created_on")
    val createdOn: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "external_id")
    val externalId: String
)