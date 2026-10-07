package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FloodViewModel
import com.example.ui.components.InteractiveFloodMap
import com.example.ui.theme.StatusAdvisory
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning

@Composable
fun MapScreen(
    viewModel: FloodViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.filteredStations.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_screen_root")
    ) {
        InteractiveFloodMap(
            stations = stations,
            selectedStation = selectedStation,
            onStationSelect = { id ->
                viewModel.selectStation(id)
            },
            onNavigateToDetails = { id ->
                onNavigateToDetails(id)
            }
        )

        // Status Legend at bottom-right
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF091424).copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                MapLegendItem(color = StatusCritical, label = "Critical Breach")
                MapLegendItem(color = StatusWarning, label = "Warning Surge")
                MapLegendItem(color = StatusAdvisory, label = "Advisory Flow")
                MapLegendItem(color = StatusNormal, label = "Normal Stage")
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
