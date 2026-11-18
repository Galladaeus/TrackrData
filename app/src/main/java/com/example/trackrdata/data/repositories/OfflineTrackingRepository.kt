package com.example.trackrdata.data.repositories

import androidx.room.withTransaction
import com.example.trackrdata.data.AppDatabase
import com.example.trackrdata.data.dao.LocationDao
import com.example.trackrdata.data.dao.VisitDao
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit
import kotlinx.coroutines.flow.Flow

class OfflineTrackingRepository(
    private val database: AppDatabase
) : TrackingRepository {
    private val locationDao = database.locationDao()
    private val visitDao = database.visitDao()
    private val locationSampleDao = database.locationSampleDao()

    override fun getAllLocationsStream(): Flow<List<Location>> = locationDao.observeAll()

    override fun getAllVisitsStream(): Flow<List<Visit>> = visitDao.observeAll()

    override fun getLocationStream(id: Int): Flow<Location?> = locationDao.observedById(id)

    override fun observeVisitsForLocation(locationId: Int): Flow<List<Visit>> =
        visitDao.observeVisitsForLocation(locationId)

    override suspend fun insertLocation(location: Location) = locationDao.insertLocations(location)

    override suspend fun deleteLocation(location: Location) = locationDao.delete(location)

    override suspend fun updateLocation(location: Location) = locationDao.update(location)

    override suspend fun recordVisit(locationId: Int) {
        database.withTransaction {
            val location = locationDao.getById(locationId)
                ?: throw IllegalArgumentException(
                    "Location with id $locationId does not exist"
                )

            val now = System.currentTimeMillis()
            val activeVisits = visitDao.getActiveVisits()
            check(activeVisits.all { it.arrivedAt <= now }) {
                "An ongoing visit has an arrival time in the future."
            }

            visitDao.endActiveVisits(departedAt = now)

            visitDao.insertVisits(
                Visit(
                    name = location.name,
                    locationUid = location.uid,
                    arrivedAt = now,
                    manualEntry = true
                )
            )
        }
    }

    override suspend fun endVisit(visitId: Int, departedAt: Long) {
        val updatedRows = visitDao.endVisit(visitId, departedAt)

        check(updatedRows == 1) {
            "Visit could not be ended. It may already be complete, " +
                    "missing, or have an arrival time after the departure"
        }
    }

    override suspend fun beginAutomaticVisit(locationId: Int, arrivedAt: Long): Long {
        return database.withTransaction {
            val location = locationDao.getById(locationId)
                ?: throw IllegalArgumentException("Location no longer exists.")

            check(visitDao.getActiveVisits().isEmpty()) {
                "End existing visits before starting automatic visits."
            }

            visitDao.insertVisit(
                Visit(
                    name = location.name,
                    locationUid = location.uid,
                    arrivedAt = arrivedAt,
                    manualEntry = false,
                    notes = "Arrival estimated from qualifying GPS readings"
                )
            )
        }
    }

    override suspend fun finishAutomaticVisit(visitId: Long, departedAt: Long, note: String) {
        val updatedRows = visitDao.finishAutomaticVisit(visitId, departedAt, note)

        check(updatedRows == 1) { "The automatic visit could not be completed" }
    }

    override suspend fun insertLocationSample(sample: LocationSample): Long = locationSampleDao.insert(sample)

    override fun getLocationSamplesStream(sessionId: Long): Flow<List<LocationSample>> = locationSampleDao.observeForSession(sessionId)

    override fun getLocationSampleCountStream(sessionId: Long): Flow<Int> = locationSampleDao.observeCountForSession(sessionId)
}