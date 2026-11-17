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
    val sampleCount: Int,
    val isConfirmed: Boolean
)

/**
 * Class to help determine if the user has logged a new visit based on proximity and dwell time
 *
 * @property radiusMeters distance in meters to nearest location in which a visit is considered
 * @property maximumAccuracyMeters maximum accuracy in meters to consider a location
 * @property dwellMillis minimum time in milliseconds to consider a location a visit (default 2 minutes)
 * @property maximumGapMillis maximum time in milliseconds between readings
 * @property requiredSampleCount the number of samples required to consider a visit
 */
class VisitDetector(
    private val radiusMeters: Double = 100.0,
    private val maximumAccuracyMeters: Float = 50f,
    private val dwellMillis: Long = 120_000L,
    private val maximumGapMillis: Long = 30_000L,
    private val requiredSampleCount: Int = 3
) {
    private var candidate: VisitCandidate? = null
    private var lastProcessedAt: Long? = null

    /**
     * Determines if the user has logged a new visit based on proximity and dwell time
     */
    fun process(
        sample: LocationSample,
        locations: List<Location>
    ): VisitCandidate? {
        val previousTimestamp = lastProcessedAt

        if (previousTimestamp != null && sample.recordedAt <= previousTimestamp) {
            return candidate
        }

        lastProcessedAt = sample.recordedAt

        if (previousTimestamp != null && sample.recordedAt - previousTimestamp > maximumGapMillis) {
            candidate = null
        }

        val accuracy = sample.accuracyMeters

        if (accuracy == null || !accuracy.isFinite() ||
            accuracy < 0f || accuracy > maximumAccuracyMeters) {
            candidate = null
            return null
        }

        val nearest = locations.map { location ->
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
            return null
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
                    sampleCount = 1,
                    isConfirmed = false
                )
            }

        candidate = updated.copy(
            isConfirmed = updated.sampleCount >= requiredSampleCount &&
                    updated.lastObservedAt - updated.firstObservedAt >= dwellMillis
        )

        return candidate
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
        val latDifference = (lat2 - lat1) * radians
        val lonDifference = (lon2 - lon1) * radians

        val a = (
                sin(latDifference / 2) * sin(latDifference / 2) +
                        cos(lat1) * cos(lat2) *
                        sin(lonDifference / 2) * sin(lonDifference / 2)
                ).coerceIn(0.0, 1.0)

        return 6_371_000.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}