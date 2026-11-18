package com.example.trackrdata.utils

import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class VisitCandidate(
    val locationId: Int,
    val firstObservedAt: Long,
    val lastObservedAt: Long,
    val sampleCount: Int
)

data class ActiveVisitDetection(
    val location: Location,
    val arrivedAt: Long,
    val lastObservedInsideAt: Long,
    val outsideSince: Long? = null,
    val outsideSampleCount: Int = 0,
    val hasObservationGap: Boolean = false
)

data class DetectedDeparture(
    val locationId: Int,
    val arrivedAt: Long,
    val departedAt: Long,
    val hasObservationGap: Boolean
)

data class VisitDetectionState(
    val candidate: VisitCandidate? = null,
    val activeVisit: ActiveVisitDetection? = null,
    val lastDeparture: DetectedDeparture? = null
)

/**
 * Class to help determine if the user has logged a new visit based on proximity and dwell time
 *
 * @property radiusMeters distance in meters to nearest location in which a visit is considered
 * @property exitRadiusMeters distance in meters from a location in which a visit is considered exited
 * @property maximumAccuracyMeters maximum accuracy in meters to consider a location
 * @property dwellMillis minimum time in milliseconds to consider a location a visit (default 2 minutes)
 * @property departureDwellMillis minimum time in milliseconds in which user is considered to have departed
 * @property maximumGapMillis maximum time in milliseconds between readings
 * @property requiredSampleCount the number of samples required to consider a visit
 */
class VisitDetector(
    private val radiusMeters: Double = 100.0,
    private val exitRadiusMeters: Double = 150.0,
    private val maximumAccuracyMeters: Float = 50f,
    private val dwellMillis: Long = 120_000L,
    private val departureDwellMillis: Long = 60_000L,
    private val maximumGapMillis: Long = 30_000L,
    private val requiredSampleCount: Int = 3
) {
    private var candidate: VisitCandidate? = null
    private var activeVisit: ActiveVisitDetection? = null
    private var lastDeparture: DetectedDeparture? = null
    private var lastProcessedAt: Long? = null

    /**
     * Determines if the user has logged a new visit based on proximity and dwell time
     */
    fun process(
        sample: LocationSample,
        locations: List<Location>
    ): VisitDetectionState {
        val previousTimestamp = lastProcessedAt

        if (previousTimestamp != null && sample.recordedAt <= previousTimestamp) {
            return snapshot()
        }

        lastProcessedAt = sample.recordedAt

        if (previousTimestamp != null && sample.recordedAt - previousTimestamp > maximumGapMillis) {
            resetPendingEvidence()
        }

        val accuracy = sample.accuracyMeters

        if (accuracy == null ||
            !accuracy.isFinite() ||
            accuracy < 0f ||
            accuracy > maximumAccuracyMeters ||
            !sample.latitude.isFinite() ||
            !sample.longitude.isFinite() ||
            sample.latitude !in -90.0..90.0 ||
            sample.longitude !in -180.0..180.0
        ) {
            resetPendingEvidence()
            return snapshot()
        }

        val currentActive = activeVisit

        if (currentActive != null) {
            val distance = distanceMeters(
                sample.latitude,
                sample.longitude,
                currentActive.location.latitude,
                currentActive.location.longitude
            )

            if (distance <= exitRadiusMeters) {
                activeVisit = currentActive.copy(
                    lastObservedInsideAt = sample.recordedAt,
                    outsideSince = null,
                    outsideSampleCount = 0
                )

                return snapshot()
            }

            val outsideSince = currentActive.outsideSince ?: sample.recordedAt
            val outsideCount = currentActive.outsideSampleCount + 1
            val departureConfirmed = outsideCount >= requiredSampleCount &&
                    sample.recordedAt - outsideSince >= departureDwellMillis

            if (!departureConfirmed) {
                activeVisit = currentActive.copy(
                    outsideSince = outsideSince,
                    outsideSampleCount = outsideCount
                )

                return snapshot()
            }

            lastDeparture = DetectedDeparture(
                locationId = currentActive.location.uid,
                arrivedAt = currentActive.arrivedAt,
                departedAt = outsideSince,
                hasObservationGap = currentActive.hasObservationGap
            )

            activeVisit = null
            candidate = null
        }

        val nearest = locations
            .map { location ->
                location to distanceMeters(
                    sample.latitude,
                    sample.longitude,
                    location.latitude,
                    location.longitude
                )
            }
            .minByOrNull { it.second }

        if (nearest == null || nearest.second > radiusMeters) {
            candidate = null
            return snapshot()
        }

        val location = nearest.first
        val previousCandidate = candidate

        val updated =
            if (previousCandidate?.locationId == location.uid) {
                previousCandidate.copy(
                    lastObservedAt = sample.recordedAt,
                    sampleCount = previousCandidate.sampleCount + 1
                )
            } else {
                VisitCandidate(
                    locationId = location.uid,
                    firstObservedAt = sample.recordedAt,
                    lastObservedAt = sample.recordedAt,
                    sampleCount = 1
                )
            }

        val arrivalConfirmed = updated.sampleCount >= requiredSampleCount &&
                updated.lastObservedAt - updated.firstObservedAt >= dwellMillis

        if (arrivalConfirmed) {
            activeVisit = ActiveVisitDetection(
                location = location,
                arrivedAt = updated.firstObservedAt,
                lastObservedInsideAt = sample.recordedAt
            )

            candidate = null
        } else {
            candidate = updated
        }

        return snapshot()
    }

    private fun resetPendingEvidence() {
        candidate = null

        activeVisit = activeVisit?.copy(
            outsideSince = null,
            outsideSampleCount = 0,
            hasObservationGap = true
        )
    }

    private fun snapshot(): VisitDetectionState {
        return VisitDetectionState(
            candidate = candidate,
            activeVisit = activeVisit,
            lastDeparture = lastDeparture
        )

    }
}


/**
 * Calculate the distance between two points on a sphere using the Haversine formula
 */
private fun distanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val radians = Math.PI / 180.0

    val lat1 = lat1 * radians
    val lat2 = lat2 * radians
    val latDifference = (lat2 - lat1)
    val lonDifference = (lon2 - lon1) * radians

    val a = (
            sin(latDifference / 2) * sin(latDifference / 2) +
                    cos(lat1) * cos(lat2) *
                    sin(lonDifference / 2) * sin(lonDifference / 2)
            ).coerceIn(0.0, 1.0)

    return 6_371_000.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
}