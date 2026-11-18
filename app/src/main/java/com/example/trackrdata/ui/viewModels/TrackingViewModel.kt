package com.example.trackrdata.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit
import com.example.trackrdata.data.location.CurrentLocationProvider
import com.example.trackrdata.data.repositories.TrackingRepository
import com.example.trackrdata.ui.screens.LocationEntryUiState
import com.example.trackrdata.ui.screens.TrackingUiState
import com.example.trackrdata.ui.screens.VisitEntryUiState
import com.example.trackrdata.utils.AutomaticVisitRecorder
import com.example.trackrdata.utils.VisitCandidate
import com.example.trackrdata.utils.VisitDetectionState
import com.example.trackrdata.utils.VisitDetector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TrackingViewModel(
    private val repository: TrackingRepository,
    private val currentLocationProvider: CurrentLocationProvider
) : ViewModel() {
    private val _departureError = MutableStateFlow<String?>(null)
    val departureError = _departureError.asStateFlow()

    // UI state trackers
    private val _locationEntry = MutableStateFlow(LocationEntryUiState())
    val locationEntry = _locationEntry.asStateFlow()

    private val _visitEntry = MutableStateFlow(VisitEntryUiState())
    val visitEntry = _visitEntry.asStateFlow()

    private val _tracking = MutableStateFlow(TrackingUiState())
    val tracking = _tracking.asStateFlow()

    // Visit detection
    private val _visitDetection = MutableStateFlow(VisitDetectionState())
    val visitDetection = _visitDetection.asStateFlow()

    private var trackingJob: Job? = null

    val locations: StateFlow<List<Location>> =
        repository.getAllLocationsStream().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val visits: StateFlow<List<Visit>> =
        repository.getAllVisitsStream().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val trackingSamples: StateFlow<List<LocationSample>> =
        tracking
            .map { it.sessionId }
            .distinctUntilChanged()
            .flatMapLatest { sessionId ->
                if (sessionId == null) {
                    flowOf(emptyList<LocationSample>())
                } else {
                    repository.getLocationSamplesStream(sessionId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addCurrentLocation(name: String) {
        val current = _locationEntry.value
        if (current.isCapturing || current.isSaving) return

        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            _locationEntry.value = current.copy(
                message = "Enter a location name first."
            )
            return
        }

        _locationEntry.value = current.copy(
            coordinates = null,
            isCapturing = true,
            isSaving = false,
            message = null
        )

        viewModelScope.launch {
            try {
                val coordinates = currentLocationProvider.getCurrentCoordinates()

                if (coordinates == null) {
                    _locationEntry.value = _locationEntry.value.copy(
                        message = "No location found. Check that device Location is on."
                    )
                    return@launch
                }

                _locationEntry.value = _locationEntry.value.copy(
                    isCapturing = true,
                    isSaving = true
                )

                repository.insertLocation(
                    Location(
                        name = trimmedName,
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        category = "User Location",
                        address = "",
                        notes = "",
                        externalId = ""
                    )
                )

                _locationEntry.value = _locationEntry.value.copy(
                    message = "Location saved."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _locationEntry.value = _locationEntry.value.copy(
                    message = "Could not save location. Check device permissions."
                )
            } finally {
                _locationEntry.value = _locationEntry.value.copy(
                    isCapturing = false,
                    isSaving = false
                )
            }
        }
    }

    fun startVisit(locationId: Int) {
        if (_tracking.value.isTracking){
            _visitEntry.value = _visitEntry.value.copy(
                message = "Stop automatic tracking before using manual visits"
            )
            return
        }

        if (_visitEntry.value.isSaving) return

        _visitEntry.value = VisitEntryUiState(
            isSaving = true
        )

        viewModelScope.launch {
            try {
                repository.recordVisit(locationId)

                _visitEntry.value = _visitEntry.value.copy(
                    message = "Visit started."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _visitEntry.value = _visitEntry.value.copy(
                    message = "Could not start visit. Please try again."
                )
            } finally {
                _visitEntry.value = _visitEntry.value.copy(
                    isSaving = false
                )
            }
        }
    }

    fun endVisit(visitId: Int) {
        if (_tracking.value.isTracking) {
            _departureError.value = "Stop automatic tracking before using manual visits"
            return
        }

        viewModelScope.launch {
            val departedAt = System.currentTimeMillis()

            viewModelScope.launch {
                _departureError.value = null

                try {
                    repository.endVisit(visitId, departedAt)
                } catch (e: CancellationException) {
                  throw e
                } catch (e: Exception) {
                    _departureError.value = "Could not save departure. Please try again."
                }
            }
        }
    }

    fun startTracking() {
        if (trackingJob?.isCompleted == false) return

        val sessionId = UUID.randomUUID().mostSignificantBits

        val detector = VisitDetector()
        val recorder = AutomaticVisitRecorder(repository)
        _visitDetection.value = VisitDetectionState()

        _tracking.value = TrackingUiState(
            isTracking = true,
            sessionId = sessionId,
            message = "Waiting for a location..."
        )

        trackingJob = viewModelScope.launch {
            try {
                currentLocationProvider.observeCoordinates()
                    .collect { coordinates ->
                        val sample = LocationSample(
                            sessionId = sessionId,
                            latitude = coordinates.latitude,
                            longitude = coordinates.longitude,
                            accuracyMeters = coordinates.accuracyMeters,
                            recordedAt = coordinates.measuredAt
                        )

                        repository.insertLocationSample(sample)

                        val detection = detector.process(
                            sample = sample,
                            locations = locations.value
                        )
                        recorder.process(detection)
                        _visitDetection.value = detection

                        _tracking.value = _tracking.value.copy(
                            samplesRecorded = _tracking.value.samplesRecorded + 1,
                            lastCoordinates = coordinates,
                            message = null
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _tracking.value = _tracking.value.copy(
                    message = "Tracking stopped because of an error."
                )
            } finally {
                try {
                    recorder.finishSession()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _tracking.value = _tracking.value.copy(
                        message = "Tracking stopped because of an error."
                    )
                } finally {
                    _tracking.value = _tracking.value.copy( isTracking = false )
                }
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }
}