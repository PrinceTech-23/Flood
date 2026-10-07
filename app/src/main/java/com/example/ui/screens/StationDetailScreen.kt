package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.ui.components.HydrographChart
import com.example.ui.components.WaterLevelGauge
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    stationId: String,
    viewModel: FloodViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToPrediction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station by viewModel.selectedStation.collectAsState()
    val telemetry by viewModel.selectedTelemetry.collectAsState()
    val forecast by viewModel.currentForecast.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = station?.name ?: "Station Telemetry",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    station?.let { stn ->
                        IconButton(
                            onClick = { viewModel.toggleFavorite(stn.id, stn.isFavorite) },
                            modifier = Modifier.testTag("detail_fav_btn")
                        ) {
                            Icon(
                                imageVector = if (stn.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (stn.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (station == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Sensor Station not found.")
            }
        } else {
            val stn = station!!
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val lastSeenStr = timeFormat.format(Date(stn.lastReportedTime))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("station_detail_column")
            ) {
                // Station Meta Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stn.basinName,
                            style = MaterialTheme.typography.labelLarge,
                            color = FloodSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GPS: ${"%.4f".format(stn.latitude)}, ${"%.4f".format(stn.longitude)} • ID: ${stn.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Ping $lastSeenStr",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Water Level Reservoir Gauge
                WaterLevelGauge(
                    currentWaterLevelM = stn.currentWaterLevelM,
                    warningLevelM = stn.warningLevelM,
                    dangerLevelM = stn.dangerLevelM,
                    crestLimitM = stn.crestLimitM,
                    rateOfRiseCmHr = stn.rateOfRiseCmHr,
                    status = stn.status
                )

                Spacer(modifier = Modifier.height(16.dp))

                // IoT Sensor Hardware Telemetry Dashboard
                Text(
                    text = "IoT Node Hardware Architecture",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HardwareMetricItem(
                                icon = Icons.Default.Memory,
                                label = "Firmware MCU",
                                value = stn.hardwareType.take(17),
                                tint = FloodSecondary
                            )
                            HardwareMetricItem(
                                icon = Icons.Default.Sensors,
                                label = "Protocol Link",
                                value = stn.transmissionProtocol.take(15),
                                tint = Color(0xFFF59E0B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HardwareMetricItem(
                                icon = Icons.Default.BatteryChargingFull,
                                label = "Battery & Solar",
                                value = "${stn.batteryPercent}% (4.12V)",
                                tint = Color(0xFF10B981)
                            )
                            HardwareMetricItem(
                                icon = Icons.Default.SignalCellularAlt,
                                label = "Signal RSSI",
                                value = "${stn.signalDbm} dBm (LoRa)",
                                tint = FloodSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HardwareMetricItem(
                                icon = Icons.Default.Speed,
                                label = "Doppler Velocity",
                                value = "${"%.1f".format(1.2 + (stn.currentWaterLevelM / stn.crestLimitM) * 1.8)} m/s",
                                tint = Color(0xFF38BDF8)
                            )
                            HardwareMetricItem(
                                icon = Icons.Default.WbSunny,
                                label = "Tipping Gauge",
                                value = "${stn.rainfallMm24h} mm (24h)",
                                tint = Color(0xFF818CF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 12h Telemetry Hydrograph
                HydrographChart(
                    readings = telemetry,
                    warningLevelM = stn.warningLevelM,
                    dangerLevelM = stn.dangerLevelM,
                    crestLimitM = stn.crestLimitM
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Predictive Runoff & Crest Arrival Alert
                if (forecast != null) {
                    val fc = forecast!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (fc.riskRating) {
                                "EXTREME" -> StatusCritical.copy(alpha = 0.15f)
                                "HIGH" -> StatusWarning.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (fc.riskRating) {
                                "EXTREME" -> StatusCritical
                                "HIGH" -> StatusWarning
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = FloodSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Inundation Risk Model",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (fc.riskRating) {
                                        "EXTREME" -> StatusCritical
                                        "HIGH" -> StatusWarning
                                        else -> Color(0xFF10B981)
                                    }
                                ) {
                                    Text(
                                        text = "${fc.riskRating} RISK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Projected Crest: ${"%.2f".format(fc.predictedPeakLevelM)}m in +${fc.hoursToPeak} hours. Overflow Probability: ${fc.overflowProbabilityPercent}%.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onNavigateToPrediction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_full_prediction_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("View Hydrology Forecast & Mitigation")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action to jump into IoT Hardware Simulator
                Button(
                    onClick = onNavigateToSimulator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_station_spike_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FloodSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = FloodSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Sensor Surge in IoT Studio",
                        color = FloodSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HardwareMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
