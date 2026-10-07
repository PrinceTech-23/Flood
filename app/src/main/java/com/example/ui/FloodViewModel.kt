package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CitizenReportEntity
import com.example.data.local.FloodAlertEntity
import com.example.data.local.FloodDatabase
import com.example.data.local.SensorStationEntity
import com.example.data.local.TelemetryReadingEntity
import com.example.data.model.HydrologyPredictionEngine
import com.example.data.model.InundationForecast
import com.example.data.repository.FloodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FloodViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FloodDatabase.getDatabase(application)
    private val repository = FloodRepository(
        stationDao = database.sensorStationDao(),
        telemetryDao = database.telemetryReadingDao(),
        reportDao = database.citizenReportDao(),
        alertDao = database.floodAlertDao()
    )

    val activeAlerts: StateFlow<List<FloodAlertEntity>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val citizenReports: StateFlow<List<CitizenReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter = _statusFilter.asStateFlow()

    val filteredStations: StateFlow<List<SensorStationEntity>> = combine(
        repository.allStations,
        _searchQuery,
        _statusFilter
    ) { stations, query, filter ->
        stations.filter { station ->
            val matchesQuery = query.isBlank() ||
                station.name.contains(query, ignoreCase = true) ||
                station.basinName.contains(query, ignoreCase = true) ||
                station.hardwareType.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "ALL" -> true
                "FAVORITES" -> station.isFavorite
                else -> station.status == filter
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStationId = MutableStateFlow<String?>("STN-ESP32-01")
    val selectedStationId = _selectedStationId.asStateFlow()

    val selectedStation: StateFlow<SensorStationEntity?> = _selectedStationId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getStation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedTelemetry: StateFlow<List<TelemetryReadingEntity>> = _selectedStationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getReadingsForStation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Inundation Forecast for selected station
    val currentForecast: StateFlow<InundationForecast?> = selectedStation
        .combine(_selectedStationId) { station, _ ->
            station?.let {
                HydrologyPredictionEngine.calculateForecast(
                    stationName = it.name,
                    stationId = it.id,
                    currentWaterLevelM = it.currentWaterLevelM,
                    warningLevelM = it.warningLevelM,
                    dangerLevelM = it.dangerLevelM,
                    crestLimitM = it.crestLimitM,
                    rainfallMm1h = it.rainfallMm1h,
                    rainfallMm24h = it.rainfallMm24h,
                    rateOfRiseCmHr = it.rateOfRiseCmHr
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Hardware Simulation State
    private val _isLiveTelemetryPolling = MutableStateFlow(true)
    val isLiveTelemetryPolling = _isLiveTelemetryPolling.asStateFlow()

    private val _lastSimulatedPacket = MutableStateFlow<String?>(null)
    val lastSimulatedPacket = _lastSimulatedPacket.asStateFlow()

    private val _alarmTriggered = MutableStateFlow(false)
    val alarmTriggered = _alarmTriggered.asStateFlow()

    init {
        // Start automatic periodic sensor jitter to simulate live IoT transmission
        startLiveTelemetryPolling()
    }

    private fun startLiveTelemetryPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(12_000) // Poll every 12 seconds
                if (_isLiveTelemetryPolling.value) {
                    val stn = selectedStation.value
                    if (stn != null) {
                        // Micro fluctuation in ultrasonic reading (+- 1-3cm)
                        val delta = (Math.random() - 0.48) * 0.05
                        val newLevel = (stn.currentWaterLevelM + delta).coerceIn(0.5, stn.crestLimitM + 0.5)
                        repository.updateStationReading(
                            stationId = stn.id,
                            newWaterLevelM = newLevel,
                            rainfall1h = stn.rainfallMm1h,
                            rateOfRiseCmHr = stn.rateOfRiseCmHr
                        )
                        _lastSimulatedPacket.value = """
                            {
                              "devEUI": "${stn.id}",
                              "hw": "${stn.hardwareType.take(8)}",
                              "water_lvl_m": ${"%.2f".format(newLevel)},
                              "battery_pct": ${stn.batteryPercent},
                              "rssi_dbm": ${stn.signalDbm},
                              "proto": "${stn.transmissionProtocol.take(4)}"
                            }
                        """.trimIndent()
                    }
                }
            }
        }
    }

    fun selectStation(id: String) {
        _selectedStationId.value = id
    }

    fun setFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(stationId: String, currentVal: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(stationId, currentVal)
        }
    }

    fun toggleLivePolling() {
        _isLiveTelemetryPolling.value = !_isLiveTelemetryPolling.value
    }

    // IoT Hardware Simulator Controls
    fun simulateManualWaterLevel(stationId: String, newLevelM: Double) {
        viewModelScope.launch {
            val station = repository.getStationDirect(stationId) ?: return@launch
            val diffM = newLevelM - station.currentWaterLevelM
            val simulatedRate = Math.round(diffM * 60.0 * 10.0) / 10.0
            repository.updateStationReading(
                stationId = stationId,
                newWaterLevelM = newLevelM,
                rainfall1h = station.rainfallMm1h,
                rateOfRiseCmHr = simulatedRate
            )
            _lastSimulatedPacket.value = "TX LoRa PKT: ${stationId} -> Level=${"%.2f".format(newLevelM)}m (${simulatedRate}cm/h)"
        }
    }

    fun simulateRainfallBurst(stationId: String, addedRainMm: Double) {
        viewModelScope.launch {
            val station = repository.getStationDirect(stationId) ?: return@launch
            val newRain = station.rainfallMm1h + addedRainMm
            // Rain leads to surge
            val surge = (addedRainMm * 0.04)
            val newLevel = station.currentWaterLevelM + surge
            val newRise = station.rateOfRiseCmHr + (addedRainMm * 0.8)
            repository.updateStationReading(
                stationId = stationId,
                newWaterLevelM = newLevel,
                rainfall1h = newRain,
                rateOfRiseCmHr = newRise
            )
            _lastSimulatedPacket.value = "RAIN BURST EVENT: +${addedRainMm}mm -> Water Surged +${"%.2f".format(surge)}m"
        }
    }

    fun addNewSensorStation(
        name: String,
        basinName: String,
        latitude: Double,
        longitude: Double,
        warningLevel: Double,
        dangerLevel: Double,
        crestLimit: Double,
        hardwareType: String,
        protocol: String
    ) {
        viewModelScope.launch {
            val newId = repository.addCustomStation(
                name = name,
                basinName = basinName,
                latitude = latitude,
                longitude = longitude,
                warningLevel = warningLevel,
                dangerLevel = dangerLevel,
                crestLimit = crestLimit,
                hardwareType = hardwareType,
                protocol = protocol
            )
            _selectedStationId.value = newId
        }
    }

    fun submitCitizenReport(
        title: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        waterDepthCategory: String,
        hazardType: String,
        description: String,
        reporterName: String
    ) {
        viewModelScope.launch {
            repository.submitCitizenReport(
                title = title,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                waterDepthCategory = waterDepthCategory,
                hazardType = hazardType,
                description = description,
                reporterName = reporterName
            )
        }
    }

    fun verifyCitizenReport(reportId: String) {
        viewModelScope.launch {
            repository.verifyReport(reportId)
        }
    }

    fun dismissAlert(alertId: String) {
        viewModelScope.launch {
            repository.dismissAlert(alertId)
        }
    }

    fun triggerEmergencyAlarm(context: Context) {
        _alarmTriggered.value = true
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 400, 200, 400, 200, 800)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }
        } catch (_: Exception) {
            // Ignore if vibration not permitted
        }

        viewModelScope.launch {
            delay(4000)
            _alarmTriggered.value = false
        }
    }
}
