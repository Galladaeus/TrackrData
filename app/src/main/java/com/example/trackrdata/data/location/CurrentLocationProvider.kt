package com.example.trackrdata.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class CurrentLocationProvider(context: Context) {
    private val appContext = context.applicationContext

    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): CurrentCoordinates? {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) {
            throw SecurityException("Location permission is required")
        }

        val request = CurrentLocationRequest.Builder()
            .setPriority(
                if (hasFinePermission) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }
            )
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(15_000)
            .build()

        val cancellation = CancellationTokenSource()

        return try {
            withTimeoutOrNull(20_000.milliseconds) {
                val result = client.getCurrentLocation(
                    request,
                    cancellation.token
                ).await()

                result?.let {
                    CurrentCoordinates(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracyMeters = it.accuracy,
                        measuredAt = it.time
                    )
                }
            }
        } finally {
            cancellation.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    fun observeCoordinates(): Flow<CurrentCoordinates> = callbackFlow {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) {
            throw SecurityException("Location permission is required")
        }

        val priority = if (hasFinePermission) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val request = LocationRequest.Builder(priority, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setMaxUpdateAgeMillis(0L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val coordinates = CurrentCoordinates(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        measuredAt = location.time
                    )

                    val sent = trySend(coordinates)

                    if (sent.isFailure) {
                        close(
                            IllegalStateException(
                                "Failed to send location readings."
                            )
                        )
                        return
                    }
                }
            }
        }

        client.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { error ->
            close(error)
        }

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}