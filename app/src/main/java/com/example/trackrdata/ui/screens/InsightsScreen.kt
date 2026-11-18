package com.example.trackrdata.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trackrdata.data.models.LocationSummary
import com.example.trackrdata.data.models.NearbySuggestion
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    locationSummaries: List<LocationSummary>,
    suggestions: List<NearbySuggestion>
) {
    val totalVisits = locationSummaries.sumOf { it.visitCount }
    val visitedPlaces = locationSummaries.count { it.visitCount > 0 }

    val mostVisited = locationSummaries
        .filter { it.visitCount > 0 }
        .take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Understand your travel patterns",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "All time · Includes ongoing visits",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
               InsightStat(
                   value = totalVisits.toString(),
                   label = "Recorded visits",
                   modifier = Modifier.weight(1f)
               )

                InsightStat(
                    value = visitedPlaces.toString(),
                    label = "Visited places",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Most visited",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (mostVisited.isEmpty()) {
            item { Text("Record some visits to see your frequent places.") }
        }

        items(
            items = mostVisited,
            key = { "popular_${it.location.uid}" }
        ) { summary ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = summary.location.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("${summary.visitCount} recorded visits")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Travel opportunities",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Nearby places you visit frequently",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (suggestions.isEmpty()) {
            item {
                Text(
                    "No nearby pairs qualify yet. Each place needs " +
                        "at least 2 recorded visits and must be 50m - 1km apart."
                )
            }
        }

        items(
            items = suggestions,
            key = {"pair_${it.first.location.uid}_${it.second.location.uid}"}
        ) { suggestion ->
            NearbySuggestionCard(suggestion)
        }
    }
}

@Composable
private fun InsightStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NearbySuggestionCard(
    suggestion: NearbySuggestion
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Consider combining nearby stops",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${suggestion.first.location.name}\n" +
                        suggestion.second.location.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Approximately ${suggestion.distanceMeters.roundToInt()} m apart · straight-line"
            )

            Text(
                "${suggestion.first.visitCount} and " +
                    "${suggestion.second.visitCount} recorded visits"
            )

            Text(
                text = "Could these stops fit into one outing?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}