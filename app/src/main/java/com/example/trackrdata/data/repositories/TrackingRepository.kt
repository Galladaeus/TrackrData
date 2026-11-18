package com.example.trackrdata.data.repositories

import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    fun getAllLocationsStream(): Flow<List<Location>>

    fun getAllVisitsStream(): Flow<List<Visit>>

    fun getLocationStream(id: Int): Flow<Location?>

    fun observeVisitsForLocation(locationId: Int): Flow<List<Visit>>

    suspend fun insertLocation(location: Location)

    suspend fun deleteLocation(location: Location)

    suspend fun updateLocation(location: Location)

    suspend fun recordVisit(locationId: Int)

    suspend fun endVisit(visitId: Int, departedAt: Long)

    suspend fun insertLocationSample(sample: LocationSample): Long

    fun getLocationSamplesStream(sessionId: Long): Flow<List<LocationSample>>

    fun getLocationSampleCountStream(sessionId: Long): Flow<Int>

    suspend fun beginAutomaticVisit(locationId: Int, arrivedAt: Long): Long

    suspend fun finishAutomaticVisit(visitId: Long, departedAt: Long, note: String)
}