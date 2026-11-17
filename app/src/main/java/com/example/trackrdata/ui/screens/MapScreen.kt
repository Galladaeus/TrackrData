package com.example.trackrdata.ui.screens

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
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.Visit

fun createMap(): ArcGISMap {
    return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
        initialViewpoint = Viewpoint(
            latitude = 38.986,
            longitude = -76.942,
            scale = 100000.0
        )
    }
}

@Composable
fun MapScreen(
    map: ArcGISMap,
    locations: List<Location>,
    visits: List<Visit>,
    modifier: Modifier = Modifier
) {
    val graphicsOverlay = remember { GraphicsOverlay() }

    LaunchedEffect(locations) {
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

    MapView(
        arcGISMap = map,
        graphicsOverlays = listOf(graphicsOverlay),
        modifier = modifier.fillMaxSize()
    )
}
