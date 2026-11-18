package com.example.trackrdata.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.models.LocationSummary
import com.example.trackrdata.ui.screens.state.LocationEntryUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun LocationsScreen(
    locationSummaries: List<LocationSummary>,
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

    val totalVisits = locationSummaries.sumOf { it.visitCount }
    val visitedPlaces = locationSummaries.count { it.visitCount > 0 }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Locations", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "$totalVisits recorded visits · $visitedPlaces visited places",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "All time · Most visited first",
                style = MaterialTheme.typography.labelMedium
            )
            Text("Enter location name to add current location.")

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

            if (locationSummaries.isEmpty()) {
                Text("No saved locations yet.")
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = locationSummaries,
                    key = { it.location.uid }
                ) { summary ->
                    val location = summary.location

                    LocationSummaryCard(summary = summary)
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

@Composable
private fun LocationSummaryCard(summary: LocationSummary) {
    val location = summary.location

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = location.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (location.address.isNotBlank()) {
                        Text(
                            text = location.address,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                LocationMetadata(
                    icon = Icons.Default.DateRange,
                    text = "${summary.visitCount} " + if (summary.visitCount == 1) "visit" else "visits",
                    modifier = Modifier.weight(1f)
                )

                LocationMetadata(
                    icon = Icons.Default.Refresh,
                    text = formatLastVisited(summary.lastVisitedAt),
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun LocationMetadata(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatLastVisited(timestamp: Long?): String {
    if (timestamp == null) return "Not visited yet"

    val date = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

    return "Last visited $date"
}