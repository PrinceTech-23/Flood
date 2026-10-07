package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CitizenReportsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.PredictionScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.screens.StationDetailScreen
import com.example.ui.theme.FloodSecondary
import com.example.ui.theme.StatusCritical

enum class FloodTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    DASHBOARD("Stations", Icons.Default.Water, "tab_dashboard"),
    MAP("River Map", Icons.Default.Map, "tab_map"),
    PREDICTION("Forecast", Icons.Default.Analytics, "tab_prediction"),
    SIMULATOR("IoT Lab", Icons.Default.Sensors, "tab_simulator"),
    REPORTS("Reports", Icons.Default.ReportProblem, "tab_reports"),
    EMERGENCY("SOS", Icons.Default.CrisisAlert, "tab_emergency")
}

@Composable
fun FloodWatchApp(
    viewModel: FloodViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(FloodTab.DASHBOARD) }
    var viewingStationId by remember { mutableStateOf<String?>(null) }

    val activeAlerts by viewModel.activeAlerts.collectAsState()

    // Handle system back navigation if inspecting a station detail
    if (viewingStationId != null) {
        BackHandler {
            viewingStationId = null
        }
    } else if (currentTab != FloodTab.DASHBOARD) {
        BackHandler {
            currentTab = FloodTab.DASHBOARD
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (viewingStationId == null) {
                NavigationBar(
                    modifier = Modifier.testTag("main_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    FloodTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                viewingStationId = null
                            },
                            icon = {
                                if (tab == FloodTab.EMERGENCY && activeAlerts.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = StatusCritical
                                            ) {
                                                Text("${activeAlerts.size}")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(tab.testTag),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FloodSecondary,
                                selectedTextColor = FloodSecondary,
                                indicatorColor = FloodSecondary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (viewingStationId != null) {
            StationDetailScreen(
                stationId = viewingStationId!!,
                viewModel = viewModel,
                onNavigateBack = { viewingStationId = null },
                onNavigateToSimulator = {
                    viewingStationId = null
                    currentTab = FloodTab.SIMULATOR
                },
                onNavigateToPrediction = {
                    viewingStationId = null
                    currentTab = FloodTab.PREDICTION
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            when (currentTab) {
                FloodTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> viewingStationId = id },
                    onNavigateToSimulator = { currentTab = FloodTab.SIMULATOR },
                    modifier = Modifier.padding(innerPadding)
                )
                FloodTab.MAP -> MapScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> viewingStationId = id },
                    modifier = Modifier.padding(innerPadding)
                )
                FloodTab.PREDICTION -> PredictionScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                FloodTab.SIMULATOR -> SimulatorScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                FloodTab.REPORTS -> CitizenReportsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                FloodTab.EMERGENCY -> EmergencyScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
