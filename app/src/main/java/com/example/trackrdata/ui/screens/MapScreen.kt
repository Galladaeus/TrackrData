package com.example.trackrdata.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arcgismaps.Color
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit

val STARTING_SCALE = 30_000.0

fun createMap(): ArcGISMap {
    return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
        initialViewpoint = Viewpoint(
            latitude = 38.986,
            longitude = -76.942,
            scale = STARTING_SCALE
        )
    }
}

@Composable
fun MapScreen(
    map: ArcGISMap,
    locations: List<Location>,
    visits: List<Visit>,
    samples: List<LocationSample>,
    modifier: Modifier = Modifier
) {
    val graphicsOverlay = remember { GraphicsOverlay() }
    val samplesOverlay = remember { GraphicsOverlay() }

    val sampleSymbol = remember {
        SimpleMarkerSymbol(
            SimpleMarkerSymbolStyle.Circle,
            Color.fromRgba(33, 150, 243, 160),
            6f
        )
    }

    // Set the map location to the user's location
    val mapViewProxy = remember { MapViewProxy() }
    val firstSample = samples.firstOrNull()
    LaunchedEffect(firstSample?.id) {
        if (firstSample != null) {
            mapViewProxy.setViewpointCenter(
                center = Point(
                    firstSample.longitude,
                    firstSample.latitude,
                    SpatialReference.wgs84()
                ),
                scale = STARTING_SCALE
            ).onFailure { error ->
                Log.w("TrackrData", "Could not set initial viewpoint", error)
            }
        }
    }

    // Display locations and visits on the map as transparent
    // red circles that grow with the number of visits.
    LaunchedEffect(locations, visits) {
        val visitCounts = visits
            .groupingBy { it.locationUid }
            .eachCount()

        val graphics = locations.map { location ->
            val visitCount = visitCounts[location.uid] ?: 0

            val markerSize = (12f + visitCount * 3f)
                .coerceAtMost(42f)

            val markerSymbol = SimpleMarkerSymbol(
                SimpleMarkerSymbolStyle.Circle,
                Color.fromRgba(255, 0, 0, 180),
                markerSize
            )

            val point = Point(
                location.longitude,
                location.latitude,
                SpatialReference.wgs84()
            )

            Graphic(
                geometry = point,
                symbol = markerSymbol
            ).apply {
                attributes["locationId"] = location.uid
                attributes["name"] = location.name
                attributes["visitCount"] = visitCount
            }
        }

        graphicsOverlay.graphics.clear()
        graphicsOverlay.graphics.addAll(graphics)
    }

    // Show points for user location
    LaunchedEffect(samples) {
        val sampleGraphics = samples.map { sample ->
            val point = Point(
                sample.longitude,
                sample.latitude,
                SpatialReference.wgs84()
            )

            Graphic(
                geometry = point,
                symbol = sampleSymbol
            ).apply {
                attributes["sampleId"] = sample.id
                attributes["recordedAt"] = sample.recordedAt
            }
        }

        samplesOverlay.graphics.clear()
        samplesOverlay.graphics.addAll(sampleGraphics)
    }

    MapView(
        arcGISMap = map,
        mapViewProxy = mapViewProxy,
        graphicsOverlays = listOf(graphicsOverlay, samplesOverlay),
        modifier = modifier.fillMaxSize()
    )
}
