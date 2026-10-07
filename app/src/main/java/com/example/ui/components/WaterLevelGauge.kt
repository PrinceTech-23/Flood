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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusAdvisory
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning

@Composable
fun WaterLevelGauge(
    currentWaterLevelM: Double,
    warningLevelM: Double,
    dangerLevelM: Double,
    crestLimitM: Double,
    rateOfRiseCmHr: Double,
    status: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        "CRITICAL" -> StatusCritical
        "WARNING" -> StatusWarning
        "ADVISORY" -> StatusAdvisory
        else -> StatusNormal
    }

    val fraction = (currentWaterLevelM / crestLimitM).coerceIn(0.0, 1.1).toFloat()
    val warningFraction = (warningLevelM / crestLimitM).coerceIn(0.0, 1.0).toFloat()
    val dangerFraction = (dangerLevelM / crestLimitM).coerceIn(0.0, 1.0).toFloat()

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_level_gauge_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with status badge & rate of rise
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                // Rate of rise indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when {
                            rateOfRiseCmHr > 1.0 -> Icons.Default.ArrowUpward
                            rateOfRiseCmHr < -1.0 -> Icons.Default.ArrowDownward
                            else -> Icons.Default.Remove
                        }
                        val rateColor = when {
                            rateOfRiseCmHr > 8.0 -> StatusCritical
                            rateOfRiseCmHr > 3.0 -> StatusWarning
                            rateOfRiseCmHr < 0 -> StatusNormal
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Trend",
                            tint = rateColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (rateOfRiseCmHr > 0) "+" else ""}${"%.1f".format(rateOfRiseCmHr)} cm/h",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = rateColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Visual Reservoir Tank Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("reservoir_canvas")
                ) {
                    val w = size.width
                    val h = size.height
                    val cornerRad = 16.dp.toPx()

                    // Background well
                    drawRoundRect(
                        color = Color(0xFF091424),
                        size = size,
                        cornerRadius = CornerRadius(cornerRad, cornerRad)
                    )

                    // Draw Threshold guideline lines
                    val warnY = h * (1f - warningFraction)
                    val dangerY = h * (1f - dangerFraction)

                    // Warning Line
                    drawLine(
                        color = StatusWarning.copy(alpha = 0.6f),
                        start = Offset(0f, warnY),
                        end = Offset(w, warnY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Danger Line
                    drawLine(
                        color = StatusCritical.copy(alpha = 0.8f),
                        start = Offset(0f, dangerY),
                        end = Offset(w, dangerY),
                        strokeWidth = 3f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
                    )

                    // Animated Water Wave Fill
                    val waterY = h * (1f - fraction)
                    val wavePath = Path()
                    wavePath.moveTo(0f, h)
                    wavePath.lineTo(0f, waterY)

                    val waveAmp = if (fraction > 0.05f) 8f else 0f
                    val waveFreq = 2.5f

                    for (x in 0..w.toInt() step 8) {
                        val angle = (x / w) * (2f * Math.PI.toFloat() * waveFreq) + waveOffset
                        val yOffset = Math.sin(angle.toDouble()).toFloat() * waveAmp
                        wavePath.lineTo(x.toFloat(), waterY + yOffset)
                    }
                    wavePath.lineTo(w, h)
                    wavePath.close()

                    // Water gradient based on status
                    val waterGradient = Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.85f),
                            Color(0xFF005082),
                            Color(0xFF03224C)
                        ),
                        startY = waterY,
                        endY = h
                    )
                    drawPath(wavePath, brush = waterGradient)

                    // Tank border stroke
                    drawRoundRect(
                        color = Color(0xFF334155),
                        size = size,
                        cornerRadius = CornerRadius(cornerRad, cornerRad),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Metric display overlay inside tank
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "${"%.2f".format(currentWaterLevelM)} m",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Water Elevation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    val clearance = (crestLimitM - currentWaterLevelM).coerceAtLeast(0.0)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "Clearance to Crest: ${"%.2f".format(clearance)}m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (clearance < 0.5) StatusCritical else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reference Threshold Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThresholdTag(label = "Warning", valueM = warningLevelM, color = StatusWarning)
                ThresholdTag(label = "Danger", valueM = dangerLevelM, color = StatusCritical)
                ThresholdTag(label = "Crest Wall", valueM = crestLimitM, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun ThresholdTag(label: String, valueM: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${"%.2f".format(valueM)}m",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
