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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FloodViewModel
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: FloodViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.filteredStations.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val isPolling by viewModel.isLiveTelemetryPolling.collectAsState()
    val lastPacket by viewModel.lastSimulatedPacket.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentStn = selectedStation ?: stations.firstOrNull()
    var sliderWaterLevel by remember(currentStn?.id) {
        mutableDoubleStateOf(currentStn?.currentWaterLevelM ?: 3.0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("simulator_screen_column")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "IoT Hardware Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ESP32 / NodeMCU ultrasonic telemetry sandbox",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Polling Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPolling) "Stream ON" else "Paused",
                    fontSize = 11.sp,
                    color = if (isPolling) StatusNormal else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = isPolling,
                    onCheckedChange = { viewModel.toggleLivePolling() },
                    modifier = Modifier.testTag("toggle_polling_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Station Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = currentStn?.let { "${it.name} (${it.id})" } ?: "Select Station",
                onValueChange = {},
                readOnly = true,
                label = { Text("Active Target IoT Node") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("select_node_dropdown")
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
                            sliderWaterLevel = stn.currentWaterLevelM
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentStn != null) {
            // Interactive Ultrasonic Water Level Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ultrasonic Transducer Reading",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.2f".format(sliderWaterLevel)} m",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                sliderWaterLevel >= currentStn.dangerLevelM -> StatusCritical
                                sliderWaterLevel >= currentStn.warningLevelM -> StatusWarning
                                else -> StatusNormal
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Adjust virtual water elevation to trigger warning/critical state and notify responders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderWaterLevel.toFloat(),
                        onValueChange = { sliderWaterLevel = it.toDouble() },
                        onValueChangeFinished = {
                            viewModel.simulateManualWaterLevel(currentStn.id, sliderWaterLevel)
                        },
                        valueRange = 0.5f..7.0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("water_level_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = FloodSecondary,
                            activeTrackColor = FloodSecondary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.5m (Dry bed)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Warning: ${currentStn.warningLevelM}m", fontSize = 10.sp, color = StatusWarning)
                        Text("Danger: ${currentStn.dangerLevelM}m", fontSize = 10.sp, color = StatusCritical)
                        Text("7.0m", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rainfall Storm Inundation Injector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tipping-Bucket Rain Gauge Surge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Inject artificial precipitation spikes into watershed hydrograph.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateRainfallBurst(currentStn.id, 10.0) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rain_10_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+10mm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { viewModel.simulateRainfallBurst(currentStn.id, 25.0) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rain_25_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+25mm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { viewModel.simulateRainfallBurst(currentStn.id, 50.0) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("rain_50_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusCritical.copy(alpha = 0.2f))
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = StatusCritical)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+50mm", fontSize = 12.sp, color = StatusCritical, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Raw LoRaWAN / MQTT Telemetry Packet Terminal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF050B14)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RAW TELEMETRY INGESTION BUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "${currentStn.transmissionProtocol.take(8)}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF091424)
                    ) {
                        Text(
                            text = lastPacket ?: """
                                {
                                  "devEUI": "${currentStn.id}",
                                  "mcu": "${currentStn.hardwareType.take(8)}",
                                  "water_level_m": ${currentStn.currentWaterLevelM},
                                  "battery_pct": ${currentStn.batteryPercent},
                                  "rssi_dbm": ${currentStn.signalDbm},
                                  "proto": "${currentStn.transmissionProtocol.take(4)}"
                                }
                            """.trimIndent(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF7DD3FC),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action: Register New Hardware Field Node
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_register_node_dialog_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register New Field IoT Sensor Node", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Add Sensor Station Dialog
    if (showAddDialog) {
        var nodeName by remember { mutableStateOf("") }
        var basinName by remember { mutableStateOf("Cedar River Basin") }
        var warningM by remember { mutableStateOf("3.8") }
        var dangerM by remember { mutableStateOf("4.8") }
        var crestM by remember { mutableStateOf("5.5") }
        var hardwareType by remember { mutableStateOf("ESP32-S3 + JSN-SR04T") }
        var protocol by remember { mutableStateOf("LoRa 868MHz (SX1262)") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Deploy IoT Sensor Node", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = nodeName,
                        onValueChange = { nodeName = it },
                        label = { Text("Station / Bridge Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = basinName,
                        onValueChange = { basinName = it },
                        label = { Text("River Basin / Drainage Area") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = warningM,
                            onValueChange = { warningM = it },
                            label = { Text("Warning (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dangerM,
                            onValueChange = { dangerM = it },
                            label = { Text("Danger (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = crestM,
                            onValueChange = { crestM = it },
                            label = { Text("Crest (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hardwareType,
                        onValueChange = { hardwareType = it },
                        label = { Text("Hardware MCU & Transducer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = protocol,
                        onValueChange = { protocol = it },
                        label = { Text("Telemetry Protocol (LoRa/GSM/WiFi)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nodeName.isNotBlank()) {
                            viewModel.addNewSensorStation(
                                name = nodeName,
                                basinName = basinName,
                                latitude = 41.9700 + (Math.random() - 0.5) * 0.05,
                                longitude = -91.6500 + (Math.random() - 0.5) * 0.05,
                                warningLevel = warningM.toDoubleOrNull() ?: 3.8,
                                dangerLevel = dangerM.toDoubleOrNull() ?: 4.8,
                                crestLimit = crestM.toDoubleOrNull() ?: 5.5,
                                hardwareType = hardwareType,
                                protocol = protocol
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_new_node_btn")
                ) {
                    Text("Deploy Node")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
