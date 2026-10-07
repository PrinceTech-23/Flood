package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FloodViewModel
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    viewModel: FloodViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.filteredStations.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val forecast by viewModel.currentForecast.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("prediction_screen_column")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hydrological Risk Model",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Predictive rainfall runoff & crest arrival forecasting",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = FloodSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Station Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedStation?.let { "${it.name} (${it.status})" } ?: "Select Station",
                onValueChange = {},
                readOnly = true,
                label = { Text("Selected River Gauge") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("prediction_station_dropdown")
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                stations.forEach { stn ->
                    DropdownMenuItem(
                        text = { Text("${stn.name} (${stn.status})") },
                        onClick = {
                            viewModel.selectStation(stn.id)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (forecast != null) {
            val fc = forecast!!
            val riskColor = when (fc.riskRating) {
                "EXTREME" -> StatusCritical
                "HIGH" -> StatusWarning
                "MODERATE" -> Color(0xFFF59E0B)
                else -> StatusNormal
            }

            // Top Hazard Assessment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Inundation Threat Level",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${fc.riskRating} INUNDATION RISK",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = riskColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = riskColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDamage,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${fc.overflowProbabilityPercent}% Overflow",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (fc.overflowProbabilityPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = riskColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Peak Prediction Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ForecastMetricCard(
                    title = "Projected Peak",
                    value = "${"%.2f".format(fc.predictedPeakLevelM)}m",
                    sub = "Crest Wall: ${fc.crestLimitM}m",
                    icon = Icons.Default.Waves,
                    tint = riskColor,
                    modifier = Modifier.weight(1f)
                )

                ForecastMetricCard(
                    title = "Time to Peak",
                    value = "+${fc.hoursToPeak}h",
                    sub = "Arrival Horizon",
                    icon = Icons.Default.HourglassTop,
                    tint = FloodSecondary,
                    modifier = Modifier.weight(1f)
                )

                ForecastMetricCard(
                    title = "Runoff Rate",
                    value = "${fc.estimatedRunoffCubicMetersPerSec}",
                    sub = "m³/sec flow",
                    icon = Icons.Default.WaterDamage,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Projected 12-Hour Hydrograph Curve
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Forecast Hydrograph Curve (+12h)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SCS-CN Unit Runoff Hydrograph Simulation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val curve = fc.hourlyForecastCurve
                    val maxVal = (fc.crestLimitM * 1.1).coerceAtLeast(6.0)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("forecast_curve_canvas")
                    ) {
                        val w = size.width
                        val h = size.height

                        // Grid
                        for (i in 0..4) {
                            val y = h * (i / 4f)
                            drawLine(
                                color = Color(0xFF334155).copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        // Crest Wall guideline
                        val crestY = h * (1f - (fc.crestLimitM / maxVal).toFloat())
                        drawLine(
                            color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                            start = Offset(0f, crestY),
                            end = Offset(w, crestY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )

                        // Path
                        val linePath = Path()
                        val fillPath = Path()

                        val firstX = 0f
                        val firstY = h * (1f - (curve.first().waterLevelM / maxVal).toFloat())
                        linePath.moveTo(firstX, firstY)
                        fillPath.moveTo(firstX, h)
                        fillPath.lineTo(firstX, firstY)

                        for (i in 1 until curve.size) {
                            val prevX = ((i - 1).toFloat() / (curve.size - 1)) * w
                            val prevY = h * (1f - (curve[i - 1].waterLevelM / maxVal).toFloat())
                            val curX = (i.toFloat() / (curve.size - 1)) * w
                            val curY = h * (1f - (curve[i].waterLevelM / maxVal).toFloat())

                            val midX = (prevX + curX) / 2f
                            linePath.cubicTo(midX, prevY, midX, curY, curX, curY)
                            fillPath.cubicTo(midX, prevY, midX, curY, curX, curY)
                        }

                        fillPath.lineTo(w, h)
                        fillPath.close()

                        // Gradient
                        val gradient = Brush.verticalGradient(
                            colors = listOf(
                                riskColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                        drawPath(fillPath, brush = gradient)

                        drawPath(
                            path = linePath,
                            color = riskColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Peak marker dot
                        val peakIndex = curve.indices.maxByOrNull { curve[it].waterLevelM } ?: 0
                        val px = (peakIndex.toFloat() / (curve.size - 1)) * w
                        val py = h * (1f - (curve[peakIndex].waterLevelM / maxVal).toFloat())
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(px, py))
                        drawCircle(color = riskColor, radius = 4.dp.toPx(), center = Offset(px, py))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Now", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+3h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+6h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+9h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+12h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actionable Emergency Mitigation Directives
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = FloodSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated Mitigation Directives",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = fc.mitigationAction,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ForecastMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tint)
            Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
