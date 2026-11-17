package com.example.trackrdata.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.location.CurrentCoordinates
import com.example.trackrdata.data.location.CurrentLocationProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LocationsScreen(
    locations: List<Location>,
    entryState: LocationEntryUiState,
    onAddLocation: (String) -> Unit
) {
    val context = LocalContext.current

    var locationName by rememberSaveable { mutableStateOf("") }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var permissionPending by remember { mutableStateOf(false) }

    val isBusy = permissionPending || entryState.isCapturing || entryState.isSaving

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionPending = false

        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            locationMessage = null
            onAddLocation(locationName)
        } else {
            locationMessage =
                "Location permission is required to capture location. You can " +
                        "enable it in the app settings."
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Enter location name to add current location."
            )

            entryState.message?.let { message ->
                Text(message)
            }

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location Name") },
                singleLine = true,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            locationMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (locations.isEmpty()) {
                Text("No saved locations yet.")
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = locations, key = { it.uid }) { location ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column (
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = location.name)
                            Text(text = location.category)
                            Text(text = location.address)
                            Text("${location.latitude}, ${location.longitude}")
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = {
                if (!isBusy) {
                    if (locationName.isBlank()) {
                        locationMessage = "Enter a location name first."
                    } else {
                        val hasFinePermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        val hasCoarsePermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        locationMessage = null

                        if (hasFinePermission || hasCoarsePermission) {
                            onAddLocation(locationName)
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
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    when {
                        permissionPending -> "Permission needed..."
                        entryState.isCapturing -> "Getting location..."
                        entryState.isSaving -> "Saving..."
                        else -> "Add Location"
                    }
                )
            }
        )
    }
}