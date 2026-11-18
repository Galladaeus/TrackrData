@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackrdata

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trackrdata.ui.AppViewModelProvider
import com.example.trackrdata.ui.screens.InsightsScreen
import com.example.trackrdata.ui.screens.LocationsScreen
import com.example.trackrdata.ui.screens.MapScreen
import com.example.trackrdata.ui.screens.TrackingControls
import com.example.trackrdata.ui.screens.VisitsScreen
import com.example.trackrdata.ui.screens.createMap
import com.example.trackrdata.ui.viewModels.TrackingViewModel

private enum class AppDestination {
    Map,
    Locations,
    Visits,
    Insights
}

@Composable
fun TrackrApp(
    navController: NavHostController = rememberNavController(),
    trackingViewModel: TrackingViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
) {
    val map = remember { createMap() }

    val locations by trackingViewModel.locations.collectAsStateWithLifecycle()
    val visits by trackingViewModel.visits.collectAsStateWithLifecycle()
    val locationSummaries by trackingViewModel.locationSummaries.collectAsStateWithLifecycle()
    val nearbySuggestions by trackingViewModel.nearbySuggestions.collectAsStateWithLifecycle()

    val departureError by trackingViewModel.departureError.collectAsStateWithLifecycle()

    // UI State tracking
    val locationEntry by trackingViewModel.locationEntry.collectAsStateWithLifecycle()
    val visitEntry by trackingViewModel.visitEntry.collectAsStateWithLifecycle()
    val tracking by trackingViewModel.tracking.collectAsStateWithLifecycle()

    // User location tracking
    val trackingSamples by trackingViewModel.trackingSamples.collectAsStateWithLifecycle()
    val visibleSamples = trackingSamples.filter { it.sessionId == tracking.sessionId }
    val visitDetection by trackingViewModel.visitDetection.collectAsStateWithLifecycle()

    // Navigation
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        trackingViewModel.stopTracking()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.trackr_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.entries.forEach { destination ->
                    val destinationIcon = when (destination) {
                        AppDestination.Map -> Icons.Default.Place
                        AppDestination.Locations -> Icons.AutoMirrored.Filled.List
                        AppDestination.Visits -> Icons.Default.DateRange
                        AppDestination.Insights -> Icons.Default.Info
                    }

                    NavigationBarItem(
                        selected = currentRoute == destination.name,
                        onClick = {
                            navController.navigate(destination.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destinationIcon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(destination.name)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Map.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = AppDestination.Map.name) {
                var showHeatmap by rememberSaveable {
                    mutableStateOf(false)
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MapScreen(
                        map = map,
                        locations = locations,
                        visits = visits,
                        samples = visibleSamples,
                        showHeatmap = showHeatmap,
                        modifier = Modifier.fillMaxSize()
                    )

                    TrackingControls(
                        state = tracking,
                        onStart = trackingViewModel::startTracking,
                        onStop = trackingViewModel::stopTracking,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !showHeatmap,
                                onClick = { showHeatmap = false },
                                label = { Text("Markers") }
                            )

                            FilterChip(
                                selected = showHeatmap,
                                onClick = { showHeatmap = true },
                                label = { Text("Heatmap") }
                            )
                        }

                        if (showHeatmap) {
                            Text("Recorded visit concentration")
                            Text("All time · Includes ongoing visits")
                        }

                        if (tracking.isTracking) {
                            val active = visitDetection.activeVisit
                            val candidate = visitDetection.candidate

                            val detectionText = when {
                                active?.outsideSince != null -> {
                                    "Checking departure from ${active.location.name}..."
                                }

                                active != null -> {
                                    "Active detected visit: ${active.location.name}"
                                }

                                candidate != null -> {
                                    val place = locations.firstOrNull { it.uid == candidate.locationId }
                                    val seconds = (candidate.lastObservedAt - candidate.firstObservedAt) / 1000

                                    "Checking arrival near ${place?.name ?: "saved place"}: ${seconds}s..."
                                }

                                else -> "No visit detected."
                            }

                            Text(
                                text = detectionText,
                                modifier = Modifier.padding(8.dp)
                            )

                            if (active?.hasObservationGap == true) {
                                Text("Some observations were missing or inaccurate")
                            }

                            visitDetection.lastDeparture?.let { departure ->
                                val place = locations.firstOrNull { it.uid == departure.locationId }

                                Text("Last confirmed departure ${place?.name ?: "saved place"}")
                            }
                        }
                    }
                }
            }
            composable(route = AppDestination.Locations.name) {
                LocationsScreen(
                    locationSummaries = locationSummaries,
                    entryState = locationEntry,
                    onAddLocation = trackingViewModel::addCurrentLocation
                )
            }
            composable(route = AppDestination.Visits.name) {
                VisitsScreen(
                    visits = visits,
                    locations = locations,
                    entryState = visitEntry,
                    onStartVisit = trackingViewModel::startVisit,
                    onEndVisit = trackingViewModel::endVisit,
                    departureError = departureError
                )
            }
            composable(route = AppDestination.Insights.name) {
                InsightsScreen(
                    locationSummaries = locationSummaries,
                    suggestions = nearbySuggestions
                )
            }
        }
    }
}
