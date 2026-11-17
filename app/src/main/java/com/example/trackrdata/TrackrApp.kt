@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.trackrdata

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.example.trackrdata.ui.screens.LocationsScreen
import com.example.trackrdata.ui.screens.MapScreen
import com.example.trackrdata.ui.viewModels.TrackingViewModel
import com.example.trackrdata.ui.screens.VisitsScreen
import com.example.trackrdata.ui.screens.createMap
import com.example.trackrdata.ui.AppViewModelProvider
import com.example.trackrdata.ui.screens.TrackingControls

private enum class AppDestination {
    Map,
    Locations,
    Visits
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
    val departureError by trackingViewModel.departureError.collectAsStateWithLifecycle()
    val locationEntry by trackingViewModel.locationEntry.collectAsStateWithLifecycle()
    val visitEntry by trackingViewModel.visitEntry.collectAsStateWithLifecycle()
    val tracking by trackingViewModel.tracking.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        trackingViewModel.stopTracking()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
        bottomBar = {
            NavigationBar() {
                AppDestination.entries.forEach { destination ->
                    val destinationIcon = when (destination) {
                        AppDestination.Map -> Icons.Default.Place
                        AppDestination.Locations -> Icons.AutoMirrored.Filled.List
                        AppDestination.Visits -> Icons.Default.DateRange
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
                        }
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TrackingControls(
                        state = tracking,
                        onStart = trackingViewModel::startTracking,
                        onStop = trackingViewModel::stopTracking
                    )

                    MapScreen(
                        map = map,
                        locations = locations,
                        visits = visits,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            composable(route = AppDestination.Locations.name) {
                LocationsScreen(
                    locations = locations,
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
        }
    }
}
