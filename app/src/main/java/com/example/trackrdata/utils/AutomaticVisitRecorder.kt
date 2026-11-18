package com.example.trackrdata.utils

import com.example.trackrdata.data.repositories.TrackingRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class AutomaticVisitRecorder(
    private val repository: TrackingRepository
) {
    private var storedVisitId: Long? = null
    private var lastActive: ActiveVisitDetection? = null

    suspend fun process(state: VisitDetectionState) {
        withContext(NonCancellable) {
            val departure = state.lastDeparture
            val visitId = storedVisitId
            val previousActive = lastActive

            val matchesStoredVisit =
                departure != null &&
                previousActive != null &&
                departure.locationId == previousActive.location.uid &&
                departure.arrivedAt == previousActive.arrivedAt

            if (visitId != null && matchesStoredVisit) {
                val confirmedDeparture = requireNotNull(departure)

                val note = if (confirmedDeparture.hasObservationGap) {
                    "Departure inferred from GPS reading. Observation gaps make duration uncertain"
                } else {
                    "Departure inferred from sustained outside readings."
                }

                repository.finishAutomaticVisit(visitId, confirmedDeparture.departedAt, note)

                storedVisitId = null
                lastActive = null
            }

            val active = state.activeVisit

            if (active != null) {
                if (storedVisitId == null) {
                    storedVisitId = repository.beginAutomaticVisit(active.location.uid, active.arrivedAt)
                }

                lastActive = active
            }
        }
    }

    suspend fun finishSession() {
        withContext(NonCancellable) {
            val visitId = storedVisitId ?: return@withContext
            val active = requireNotNull(lastActive)

            repository.finishAutomaticVisit(
                visitId,
                active.lastObservedInsideAt,
                "Tracking stopped. End time is last observation, not a confirmed departure")

            storedVisitId = null
            lastActive = null
        }
    }
}