package com.example.data.repository

import com.example.data.local.CitizenReportDao
import com.example.data.local.CitizenReportEntity
import com.example.data.local.FloodAlertDao
import com.example.data.local.FloodAlertEntity
import com.example.data.local.SensorStationDao
import com.example.data.local.SensorStationEntity
import com.example.data.local.TelemetryReadingDao
import com.example.data.local.TelemetryReadingEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FloodRepository(
    private val stationDao: SensorStationDao,
    private val telemetryDao: TelemetryReadingDao,
    private val reportDao: CitizenReportDao,
    private val alertDao: FloodAlertDao
) {
    val allStations: Flow<List<SensorStationEntity>> = stationDao.getAllStations()
    val favoriteStations: Flow<List<SensorStationEntity>> = stationDao.getFavoriteStations()
    val activeAlerts: Flow<List<FloodAlertEntity>> = alertDao.getActiveAlerts()
    val allReports: Flow<List<CitizenReportEntity>> = reportDao.getAllReports()

    fun getStation(id: String): Flow<SensorStationEntity?> = stationDao.getStationById(id)
    suspend fun getStationDirect(id: String): SensorStationEntity? = stationDao.getStationDirect(id)

    fun getReadingsForStation(id: String): Flow<List<TelemetryReadingEntity>> =
        telemetryDao.getReadingsForStation(id)

    suspend fun toggleFavorite(stationId: String, currentStatus: Boolean) {
        stationDao.setFavorite(stationId, !currentStatus)
    }

    suspend fun updateStationReading(
        stationId: String,
        newWaterLevelM: Double,
        rainfall1h: Double,
        rateOfRiseCmHr: Double
    ) {
        val station = stationDao.getStationDirect(stationId) ?: return
        val calculatedStatus = when {
            newWaterLevelM >= station.dangerLevelM -> "CRITICAL"
            newWaterLevelM >= station.warningLevelM -> "WARNING"
            newWaterLevelM >= station.warningLevelM * 0.85 -> "ADVISORY"
            else -> "NORMAL"
        }

        val updated = station.copy(
            currentWaterLevelM = Math.round(newWaterLevelM * 100.0) / 100.0,
            rainfallMm1h = rainfall1h,
            rateOfRiseCmHr = rateOfRiseCmHr,
            lastReportedTime = System.currentTimeMillis(),
            status = calculatedStatus
        )
        stationDao.updateStation(updated)

        // Insert new telemetry record
        telemetryDao.insertReading(
            TelemetryReadingEntity(
                stationId = stationId,
                timestamp = System.currentTimeMillis(),
                waterLevelM = Math.round(newWaterLevelM * 100.0) / 100.0,
                rainfallMm = rainfall1h,
                flowVelocityMs = (1.0 + (newWaterLevelM / station.crestLimitM) * 2.2)
            )
        )

        // If breached danger and no recent alert, add one
        if (calculatedStatus == "CRITICAL" && station.status != "CRITICAL") {
            alertDao.insertAlert(
                FloodAlertEntity(
                    id = "ALT-${System.currentTimeMillis()}",
                    title = "CRITICAL FLOOD SURGE: ${station.name}",
                    severity = "CRITICAL",
                    affectedBasin = station.basinName,
                    message = "Water level reached ${"%.2f".format(newWaterLevelM)}m (Crest Limit: ${station.crestLimitM}m). Immediate attention required.",
                    timestamp = System.currentTimeMillis(),
                    isActive = true
                )
            )
        }
    }

    suspend fun addCustomStation(
        name: String,
        basinName: String,
        latitude: Double,
        longitude: Double,
        warningLevel: Double,
        dangerLevel: Double,
        crestLimit: Double,
        hardwareType: String,
        protocol: String
    ): String {
        val id = "NODE-${UUID.randomUUID().toString().take(6).uppercase()}"
        val initialWaterLevel = warningLevel * 0.45
        val newStation = SensorStationEntity(
            id = id,
            name = name,
            basinName = basinName,
            latitude = latitude,
            longitude = longitude,
            currentWaterLevelM = initialWaterLevel,
            warningLevelM = warningLevel,
            dangerLevelM = dangerLevel,
            crestLimitM = crestLimit,
            rainfallMm1h = 0.0,
            rainfallMm24h = 0.0,
            rateOfRiseCmHr = 0.0,
            hardwareType = hardwareType,
            transmissionProtocol = protocol,
            batteryPercent = 100,
            signalDbm = -65,
            lastReportedTime = System.currentTimeMillis(),
            status = "NORMAL",
            isFavorite = false
        )
        stationDao.insertStation(newStation)

        // Add initial telemetry point
        telemetryDao.insertReading(
            TelemetryReadingEntity(
                stationId = id,
                timestamp = System.currentTimeMillis(),
                waterLevelM = initialWaterLevel,
                rainfallMm = 0.0,
                flowVelocityMs = 0.8
            )
        )
        return id
    }

    suspend fun submitCitizenReport(
        title: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        waterDepthCategory: String,
        hazardType: String,
        description: String,
        reporterName: String
    ) {
        val report = CitizenReportEntity(
            id = "REP-${UUID.randomUUID().toString().take(6).uppercase()}",
            title = title,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            waterDepthCategory = waterDepthCategory,
            hazardType = hazardType,
            description = description,
            timestamp = System.currentTimeMillis(),
            verified = false,
            reporterName = reporterName
        )
        reportDao.insertReport(report)
    }

    suspend fun verifyReport(reportId: String) {
        reportDao.verifyReport(reportId)
    }

    suspend fun dismissAlert(alertId: String) {
        alertDao.dismissAlert(alertId)
    }
}
