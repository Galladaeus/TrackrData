package com.example.trackrdata.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.Visit
import com.example.trackrdata.ui.screens.state.VisitEntryUiState
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun VisitsScreen(
    visits: List<Visit>,
    locations: List<Location>,
    entryState: VisitEntryUiState,
    onStartVisit: (Int) -> Unit,
    onEndVisit: (Int) -> Unit,
    departureError: String?
) {
    var selectedLocationId by rememberSaveable { mutableStateOf<Int?>(null) }
    var locationMenuExpanded by remember { mutableStateOf(false) }

    val selectedLocation = locations.firstOrNull {
        it.uid == selectedLocationId
    }

    val zoneId = ZoneId.systemDefault()

    val visitsByDate = remember(visits, zoneId) {
        visits.groupBy { visit ->
            Instant.ofEpochMilli(visit.arrivedAt)
                .atZone(zoneId)
                .toLocalDate()
        }.toSortedMap(compareByDescending<LocalDate> { it })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Visits",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Visit History",
                style = MaterialTheme.typography.bodyLarge
            )

            Box {
                OutlinedButton(
                    onClick = { locationMenuExpanded = true },
                    enabled = locations.isNotEmpty() && !entryState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedLocation?.name ?: "Select Location")
                }

                DropdownMenu(
                    expanded = locationMenuExpanded,
                    onDismissRequest = { locationMenuExpanded = false }
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location.name) },
                            onClick = {
                                selectedLocationId = location.uid
                                locationMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (locations.isEmpty()) {
                Text("Save a location on the Locations tab first.")
            }

            entryState.message?.let { message ->
                Text(message)
            }

            if (departureError != null) {
                Text(
                    text = departureError,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (visits.isEmpty()) {
                Text("No visits yet.")
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                visitsByDate.forEach { (date, visitsForDate) ->
                    item(key = "date_$date") {
                        VisitDateHeader(date = date)
                    }

                    items(
                        items = visitsForDate,
                        key = { visit -> "visit_${visit.uid}" }
                    ) { visit ->
                        VisitCard(
                            visit = visit,
                            onEndVisit = { onEndVisit(visit.uid) }
                        )
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
                if (!entryState.isSaving) {
                    val location = selectedLocation

                    if (location != null) {
                        onStartVisit(location.uid)
                    } else if (locations.isNotEmpty()) {
                        locationMenuExpanded = true
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
                    if (entryState.isSaving) {
                        "Starting visit..."
                    } else {
                        "Start visit"
                    }
                )
            }
        )
    }
}

@Composable
private fun VisitDateHeader(date: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern(
        "EEEE, MMM d, uuuu",
        Locale.getDefault()
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = date.format(formatter),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun VisitCard(
    visit: Visit,
    onEndVisit: () -> Unit
) {
    val timeRange = formatVisitTimeRange(visit)
    val duration = formatVisitDuration(visit)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisitLocationIcon()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = visit.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = if (duration != null) {
                            "$timeRange ($duration)"
                        } else {
                            timeRange
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


                if (visit.manualEntry) {
                    ManualEntryLabel()
                }
            }

            if (visit.notes.isNotBlank()) {
                Text(
                    text = visit.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (visit.departedAt == null) {
                TextButton(
                    onClick = onEndVisit,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("End Visit")
                }
            }
        }
    }
}

@Composable
private fun ManualEntryLabel() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = "Manual Entry",
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun VisitLocationIcon() {
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
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatVisitTimeRange(visit: Visit): String {
    val zone = ZoneId.systemDefault()
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    val arrival = Instant.ofEpochMilli(visit.arrivedAt).atZone(zone)

    val arrivalText = arrival.format(timeFormatter)

    val departureTimestamp = visit.departedAt ?: return "$arrivalText - Not Departed"

    val departure = Instant.ofEpochMilli(departureTimestamp).atZone(zone)

    val departureText =
        if(arrival.toLocalDate() == departure.toLocalDate()) {
            departure.format(timeFormatter)
        } else {
            departure.format(
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            )
        }

    return "$arrivalText - $departureText"
}

private fun formatVisitDuration(visit: Visit): String? {
    val departure = visit.departedAt ?: return null

    if (departure < visit.arrivedAt) {
        return "Invalid visit times"
    }

    val minutes = Duration.between(
        Instant.ofEpochMilli(visit.arrivedAt),
        Instant.ofEpochMilli(departure)
    ).toMinutes()

    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return when {
        minutes == 0L -> "Less than 1 min"
        hours == 0L -> "$remainingMinutes min"
        remainingMinutes == 0L -> "$hours hr"
        else -> "$hours hr $remainingMinutes min"
    }
}