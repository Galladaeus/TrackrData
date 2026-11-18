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
import com.arcgismaps.mapping.symbology.Renderer
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.GraphicsRenderingMode
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit

const val STARTING_SCALE = 60_000.0

private fun createVisitHeatmapRenderer(): Renderer {
    val json = """
        {
            "type": "heatmap",
            "field": "visitCount",
            "blurRadius": 15,
            "minPixelIntensity": 0,
            "maxPixelIntensity": 70,
            "colorStops": [
                { "ratio": 0.0, "color": [0, 229, 255, 0] },
                { "ratio": 0.2, "color": [0, 229, 255, 100] },
                { "ratio": 0.5, "color": [57, 255, 136, 170] },
                { "ratio": 0.75, "color": [255, 210, 0, 210] },
                { "ratio": 1.0, "color": [255, 64, 129, 240] }
            ]
        }
    """.trimIndent()

    return requireNotNull(Renderer.fromJsonOrNull(json)) {
        "Could not create heatmap renderer"
    }
}

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
    showHeatmap: Boolean = false,
    modifier: Modifier = Modifier
) {
    val graphicsOverlay = remember { GraphicsOverlay() }
    val samplesOverlay = remember { GraphicsOverlay() }

    val heatmapOverlay = remember {
        GraphicsOverlay(
            renderingMode = GraphicsRenderingMode.Static
        ).apply {
            renderer = createVisitHeatmapRenderer()
            isVisible = false
        }
    }

    val sampleSymbol = remember {
        SimpleMarkerSymbol(
            SimpleMarkerSymbolStyle.Circle,
            Color.fromRgba(33, 150, 243, 160),
            6f
        )
    }

    LaunchedEffect(showHeatmap) {
        graphicsOverlay.isVisible = !showHeatmap
        heatmapOverlay.isVisible = showHeatmap
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
                color = if(visitCount == 0) {
                    Color.fromRgba(0, 180, 50, 100)
                } else {
                    Color.fromRgba(255, 0, 0, 100)
                },
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

        val heatmapGraphics = locations.mapNotNull { location ->
            val visitCount = visitCounts[location.uid] ?: 0

            if (visitCount == 0) {
                return@mapNotNull null
            }

            Graphic(
                geometry = Point(
                    location.longitude,
                    location.latitude,
                    SpatialReference.wgs84()
                )
            ).apply {
                attributes["locationId"] = location.uid
                attributes["visitCount"] = visitCount.toDouble()
            }
        }

        heatmapOverlay.graphics.clear()
        heatmapOverlay.graphics.addAll(heatmapGraphics)
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
        graphicsOverlays = listOf(
            heatmapOverlay,
            graphicsOverlay,
            samplesOverlay
        ),
        modifier = modifier.fillMaxSize()
    )
}
