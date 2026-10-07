package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SensorStationEntity
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusAdvisory
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning

@Composable
fun InteractiveFloodMap(
    stations: List<SensorStationEntity>,
    selectedStation: SensorStationEntity?,
    onStationSelect: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapLayer by remember { mutableStateOf("Basin") } // "Basin", "Risk Zones", "Gateways"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_rad"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_flood_map_box")
    ) {
        // Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("flood_map_canvas")
                .pointerInput(stations) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Calculate nearest station pin within touch radius 40dp
                        val touchRadiusPx = 40.dp.toPx()
                        var closestStation: SensorStationEntity? = null
                        var minDistance = Float.MAX_VALUE

                        stations.forEach { stn ->
                            val (normX, normY) = getNormalizedCoords(stn)
                            val pinX = normX * w
                            val pinY = normY * h
                            val dist = Math.hypot((tapOffset.x - pinX).toDouble(), (tapOffset.y - pinY).toDouble()).toFloat()
                            if (dist < touchRadiusPx && dist < minDistance) {
                                minDistance = dist
                                closestStation = stn
                            }
                        }

                        closestStation?.let {
                            onStationSelect(it.id)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Map Background (Dark Marine Basin Grid)
            drawRect(color = Color(0xFF071220))

            // Sub-grid lines for coordinates
            val gridStep = 60.dp.toPx()
            for (x in 0..w.toInt() step gridStep.toInt()) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.35f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..h.toInt() step gridStep.toInt()) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.35f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(w, y.toFloat()),
                    strokeWidth = 1f
                )
            }

            // 2. Inundation Risk Zones (if active)
            if (mapLayer == "Risk Zones") {
                // Zone A (North Dam Inundation Plain)
                val riskPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.18f)
                    cubicTo(w * 0.40f, h * 0.22f, w * 0.65f, h * 0.15f, w * 0.85f, h * 0.32f)
                    cubicTo(w * 0.75f, h * 0.55f, w * 0.35f, h * 0.45f, w * 0.15f, h * 0.18f)
                    close()
                }
                drawPath(
                    path = riskPath,
                    color = StatusCritical.copy(alpha = 0.18f)
                )
                drawPath(
                    path = riskPath,
                    color = StatusCritical.copy(alpha = 0.5f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
                )
            }

            // 3. Meandering River Channel
            val riverPath = Path().apply {
                moveTo(w * 0.10f, h * 0.15f)
                cubicTo(w * 0.30f, h * 0.28f, w * 0.45f, h * 0.18f, w * 0.52f, h * 0.38f)
                cubicTo(w * 0.58f, h * 0.55f, w * 0.35f, h * 0.65f, w * 0.48f, h * 0.85f)
            }
            // River buffer water glow
            drawPath(
                path = riverPath,
                color = Color(0xFF0284C7).copy(alpha = 0.25f),
                style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round)
            )
            // River channel main
            drawPath(
                path = riverPath,
                color = Color(0xFF0369A1),
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Tributary Creek
            val tributaryPath = Path().apply {
                moveTo(w * 0.88f, h * 0.25f)
                cubicTo(w * 0.70f, h * 0.35f, w * 0.60f, h * 0.40f, w * 0.52f, h * 0.38f)
            }
            drawPath(
                path = tributaryPath,
                color = Color(0xFF0284C7).copy(alpha = 0.6f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. LoRa Mesh Gateway link lines (if active)
            if (mapLayer == "Gateways") {
                val gwCenter = Offset(w * 0.5f, h * 0.45f)
                stations.forEach { stn ->
                    val (nx, ny) = getNormalizedCoords(stn)
                    val stnPos = Offset(nx * w, ny * h)
                    drawLine(
                        color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                        start = gwCenter,
                        end = stnPos,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }
                // Draw LoRa Central Gateway Tower
                drawCircle(color = Color(0xFF38BDF8), radius = 10.dp.toPx(), center = gwCenter)
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = gwCenter)
            }

            // 5. Draw Sensor Stations Pins
            stations.forEach { stn ->
                val (nx, ny) = getNormalizedCoords(stn)
                val pinCenter = Offset(nx * w, ny * h)
                val isCurrent = selectedStation?.id == stn.id

                val statusColor = when (stn.status) {
                    "CRITICAL" -> StatusCritical
                    "WARNING" -> StatusWarning
                    "ADVISORY" -> StatusAdvisory
                    else -> StatusNormal
                }

                // Pulsing wave for alerts
                if (stn.status == "CRITICAL" || stn.status == "WARNING") {
                    drawCircle(
                        color = statusColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius * (if (stn.status == "CRITICAL") 1.2f else 1.0f),
                        center = pinCenter
                    )
                }

                // Selection outer ring
                if (isCurrent) {
                    drawCircle(
                        color = Color.White,
                        radius = 20.dp.toPx(),
                        center = pinCenter,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Station Pin Outer
                drawCircle(
                    color = statusColor,
                    radius = if (isCurrent) 14.dp.toPx() else 11.dp.toPx(),
                    center = pinCenter
                )

                // Station Pin Inner Dot
                drawCircle(
                    color = Color.White,
                    radius = if (isCurrent) 6.dp.toPx() else 4.dp.toPx(),
                    center = pinCenter
                )
            }
        }

        // Top Layer Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Basin", "Risk Zones", "Gateways").forEach { layer ->
                FilterChip(
                    selected = mapLayer == layer,
                    onClick = { mapLayer = layer },
                    label = { Text(layer, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = when (layer) {
                                "Basin" -> Icons.Default.Layers
                                "Risk Zones" -> Icons.Default.NearMe
                                else -> Icons.Default.Sensors
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }

        // Bottom Selected Station Preview Card
        if (selectedStation != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag("map_station_preview_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedStation.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${selectedStation.basinName} • ${selectedStation.hardwareType.take(16)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val statusColor = when (selectedStation.status) {
                            "CRITICAL" -> StatusCritical
                            "WARNING" -> StatusWarning
                            "ADVISORY" -> StatusAdvisory
                            else -> StatusNormal
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = selectedStation.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${"%.2f".format(selectedStation.currentWaterLevelM)} m",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Rate: +${"%.1f".format(selectedStation.rateOfRiseCmHr)} cm/h",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onNavigateToDetails(selectedStation.id) },
                            modifier = Modifier.testTag("inspect_telemetry_btn")
                        ) {
                            Text("Inspect Station")
                        }
                    }
                }
            }
        }
    }
}

// Map real station coordinates into relative 0..1 bounding box inside our river canvas
private fun getNormalizedCoords(station: SensorStationEntity): Pair<Float, Float> {
    return when (station.id) {
        "STN-ESP32-01" -> 0.28f to 0.24f
        "STN-ESP32-02" -> 0.49f to 0.38f
        "STN-ESP32-03" -> 0.74f to 0.32f
        "STN-ESP32-04" -> 0.42f to 0.76f
        "STN-ESP32-05" -> 0.22f to 0.52f
        else -> {
            // Hash-based deterministic coordinate for custom registered stations
            val hash = station.id.hashCode()
            val x = (0.2f + (Math.abs(hash % 60) / 100f)).coerceIn(0.15f, 0.85f)
            val y = (0.2f + (Math.abs((hash / 100) % 60) / 100f)).coerceIn(0.15f, 0.85f)
            x to y
        }
    }
}
