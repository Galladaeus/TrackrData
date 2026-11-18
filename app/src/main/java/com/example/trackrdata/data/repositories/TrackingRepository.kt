package com.example.trackrdata.data.repositories

import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit
import com.example.trackrdata.data.models.LocationSummary
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    fun getAllLocationsStream(): Flow<List<Location>>

    fun getAllVisitsStream(): Flow<List<Visit>>

    suspend fun insertLocation(location: Location)

    suspend fun recordVisit(locationId: Int)

    suspend fun endVisit(visitId: Int, departedAt: Long)

    suspend fun insertLocationSample(sample: LocationSample): Long

    fun getLocationSamplesStream(sessionId: Long): Flow<List<LocationSample>>

    suspend fun beginAutomaticVisit(locationId: Int, arrivedAt: Long): Long

    suspend fun finishAutomaticVisit(visitId: Long, departedAt: Long, note: String)

    fun getLocationSummariesStream(): Flow<List<LocationSummary>>
}