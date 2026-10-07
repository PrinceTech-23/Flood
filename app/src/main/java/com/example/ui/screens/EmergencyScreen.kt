package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FloodViewModel
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning

@Composable
fun EmergencyScreen(
    viewModel: FloodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alarmTriggered by viewModel.alarmTriggered.collectAsState()

    // Grab-bag checklist items state
    val checklistItems = remember {
        mutableStateMapOf(
            "Drinking water (1 gallon/person/day for 3 days)" to true,
            "Waterproof document pouch (IDs, insurance, deeds)" to true,
            "Battery-powered NOAA weather radio" to false,
            "LED flashlights & extra alkaline batteries" to true,
            "First aid kit & essential prescription medications" to false,
            "Portable USB power bank (charged)" to false,
            "Sturdy waterproof boots and emergency rain ponchos" to false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("emergency_screen_column")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Emergency Response Center",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Civil defense evacuation protocols & early siren drill",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.CrisisAlert,
                contentDescription = null,
                tint = StatusCritical,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Evacuation Siren Drill Button Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alarmTriggered) StatusCritical.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                2.dp,
                if (alarmTriggered) StatusCritical else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (alarmTriggered) StatusCritical else FloodSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (alarmTriggered) "SIREN DRILL ACTIVE (HAPTIC ALERT)" else "Flood Siren & Haptic Drill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alarmTriggered) StatusCritical else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Simulates civil defense emergency sirens and high-amplitude emergency vibration patterns on your mobile device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerEmergencyAlarm(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trigger_siren_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alarmTriggered) Color(0xFF991B1B) else StatusCritical
                    )
                ) {
                    Icon(imageVector = Icons.Default.CrisisAlert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (alarmTriggered) "Drill Sounding..." else "Test Emergency Siren Alarm",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Emergency Hotlines
        Text(
            text = "Emergency Response Hotlines",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EmergencyHotlineCard(
                name = "Civil Defense",
                number = "911",
                icon = Icons.Default.Security,
                tint = StatusCritical,
                modifier = Modifier.weight(1f)
            )
            EmergencyHotlineCard(
                name = "River Patrol Desk",
                number = "1-800-FLOOD",
                icon = Icons.Default.Shield,
                tint = FloodSecondary,
                modifier = Modifier.weight(1f)
            )
            EmergencyHotlineCard(
                name = "Disaster Medical",
                number = "311",
                icon = Icons.Default.LocalHospital,
                tint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Evacuation Grab-Bag Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val completedCount = checklistItems.values.count { it }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Evacuation Grab-Bag Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedCount of ${checklistItems.size} items packed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (completedCount == checklistItems.size) Color(0xFF10B981) else FloodSecondary
                        )
                    }

                    if (completedCount == checklistItems.size) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                checklistItems.keys.forEach { item ->
                    val isChecked = checklistItems[item] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checklistItems[item] = it },
                            colors = CheckboxDefaults.colors(checkedColor = FloodSecondary)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Turn Around Don't Drown Safety Rules
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StatusWarning.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turn Around, Don't Drown!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusWarning
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Just 15 cm (6 inches) of rushing water can sweep an adult off their feet.\n" +
                        "• 30 cm (12 inches) of floodwater can float most cars and small SUVs.\n" +
                        "• Never drive around road closed barriers or through flooded underpasses.\n" +
                        "• If your vehicle stalls in rapidly rising water, abandon it immediately and seek higher ground.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency High-Ground Shelters
        Text(
            text = "Designated High-Ground Shelters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ShelterItem(
            name = "Cedar Valley Civic Center High Gym",
            address = "500 1st Ave NE (Elevation 245m)",
            capacity = "Capacity: 450 beds • Pet Friendly",
            distance = "1.2 km away (Clear Route)"
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShelterItem(
            name = "Northwest High School Fieldhouse",
            address = "1200 18th Ave NW (Elevation 260m)",
            capacity = "Capacity: 600 beds • Medical Staffed",
            distance = "3.4 km away (Clear Route)"
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EmergencyHotlineCard(
    name: String,
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = name, fontSize = 10.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tint)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Call", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ShelterItem(
    name: String,
    address: String,
    capacity: String,
    distance: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FloodSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = FloodSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = capacity, fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = distance,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = FloodSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}
