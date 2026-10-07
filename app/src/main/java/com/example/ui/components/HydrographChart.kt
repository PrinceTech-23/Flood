package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.data.local.TelemetryReadingEntity
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning

@Composable
fun HydrographChart(
    readings: List<TelemetryReadingEntity>,
    warningLevelM: Double,
    dangerLevelM: Double,
    crestLimitM: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hydrograph_chart_card"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hydrograph Telemetry (12h)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ultrasonic Water Height & Rain Inundation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(FloodSecondary, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Water (m)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Rain (mm)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Awaiting telemetry data stream...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val maxLevel = (crestLimitM * 1.05).coerceAtLeast(6.0)
                val sorted = readings.sortedBy { it.timestamp }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("hydrograph_canvas")
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid lines & values
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Warning & Danger guideline marks
                    val warnY = h * (1f - (warningLevelM / maxLevel).toFloat())
                    val dangerY = h * (1f - (dangerLevelM / maxLevel).toFloat())

                    drawLine(
                        color = StatusWarning.copy(alpha = 0.5f),
                        start = Offset(0f, warnY),
                        end = Offset(w, warnY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )

                    drawLine(
                        color = StatusCritical.copy(alpha = 0.6f),
                        start = Offset(0f, dangerY),
                        end = Offset(w, dangerY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
                    )

                    // Draw Rainfall Bars at bottom
                    val barWidth = (w / sorted.size) * 0.45f
                    sorted.forEachIndexed { index, reading ->
                        val x = (index.toFloat() / (sorted.size - 1).coerceAtLeast(1)) * w
                        val barHeight = (reading.rainfallMm.toFloat() * 1.8f).coerceIn(0f, h * 0.4f)
                        if (barHeight > 2f) {
                            drawRoundRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                topLeft = Offset(x - barWidth / 2f, h - barHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }
                    }

                    // Water Level Hydrograph Line & Gradient Fill
                    val linePath = Path()
                    val fillPath = Path()

                    val firstX = 0f
                    val firstY = h * (1f - (sorted.first().waterLevelM / maxLevel).toFloat())
                    linePath.moveTo(firstX, firstY)
                    fillPath.moveTo(firstX, h)
                    fillPath.lineTo(firstX, firstY)

                    for (i in 1 until sorted.size) {
                        val prevX = ((i - 1).toFloat() / (sorted.size - 1)) * w
                        val prevY = h * (1f - (sorted[i - 1].waterLevelM / maxLevel).toFloat())
                        val curX = (i.toFloat() / (sorted.size - 1)) * w
                        val curY = h * (1f - (sorted[i].waterLevelM / maxLevel).toFloat())

                        val midX = (prevX + curX) / 2f
                        linePath.cubicTo(midX, prevY, midX, curY, curX, curY)
                        fillPath.cubicTo(midX, prevY, midX, curY, curX, curY)
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Gradient under line
                    val areaBrush = Brush.verticalGradient(
                        colors = listOf(
                            FloodSecondary.copy(alpha = 0.4f),
                            Color(0xFF0369A1).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                    drawPath(fillPath, brush = areaBrush)

                    // Line stroke
                    drawPath(
                        path = linePath,
                        color = FloodSecondary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw dots on last 3 points
                    for (i in (sorted.size - 3).coerceAtLeast(0) until sorted.size) {
                        val cx = (i.toFloat() / (sorted.size - 1)) * w
                        val cy = h * (1f - (sorted[i].waterLevelM / maxLevel).toFloat())
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, cy))
                        drawCircle(color = FloodSecondary, radius = 3.dp.toPx(), center = Offset(cx, cy))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // X-Axis Time Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "-12h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "-8h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "-4h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "-1h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Now", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FloodSecondary)
                }
            }
        }
    }
}
