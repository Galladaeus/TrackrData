package com.example.trackrdata.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.trackrdata.ui.screens.state.TrackingUiState

@Composable
fun TrackingControls(
    state: TrackingUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val context = LocalContext.current

    var permissionPending by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionPending = false

        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            permissionMessage = null
            onStart()
        } else {
            permissionMessage = "Location permission is required."
        }
    }

    Column {
        Text("Tracking only runs while app is visible.")
    }

    Text( if (state.isTracking) "Tracking active" else "Tracking inactive" )

    Text("Samples saved this session: ${state.samplesRecorded}")

    Button(
        enabled = !permissionPending,
        onClick = {
            permissionMessage = null


            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                onStart()
            } else {
                permissionPending = true

                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    ) {
        Text(if (state.isTracking) "Stop Tracking" else "Start Tracking")
    }

    state.lastCoordinates?.let { coordinates ->
        Text("Latest: ${coordinates.latitude}, ${coordinates.longitude}")
    }

    state.message?.let { Text(it) }

    permissionMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error
        )
    }
}