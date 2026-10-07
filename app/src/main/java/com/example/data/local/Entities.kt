package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_stations")
data class SensorStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val basinName: String,
    val latitude: Double,
    val longitude: Double,
    val currentWaterLevelM: Double,
    val warningLevelM: Double,
    val dangerLevelM: Double,
    val crestLimitM: Double,
    val rainfallMm1h: Double,
    val rainfallMm24h: Double,
    val rateOfRiseCmHr: Double,
    val hardwareType: String, // e.g. "ESP32-S3 + JSN-SR04T"
    val transmissionProtocol: String, // "LoRa 868MHz", "GSM NB-IoT", "MQTT Wi-Fi"
    val batteryPercent: Int,
    val signalDbm: Int,
    val lastReportedTime: Long,
    val status: String, // "NORMAL", "ADVISORY", "WARNING", "CRITICAL"
    val isFavorite: Boolean = false
)

@Entity(tableName = "telemetry_readings")
data class TelemetryReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationId: String,
    val timestamp: Long,
    val waterLevelM: Double,
    val rainfallMm: Double,
    val flowVelocityMs: Double
)

@Entity(tableName = "citizen_reports")
data class CitizenReportEntity(
    @PrimaryKey val id: String,
    val title: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val waterDepthCategory: String, // "Ankle (5-15cm)", "Knee (30-50cm)", "Waist (1m+)", "Severe (Vehicle submerged)"
    val hazardType: String, // "Street Inundation", "Blocked Culvert", "River Breach", "Bridge Overflow"
    val description: String,
    val timestamp: Long,
    val verified: Boolean = false,
    val reporterName: String
)

@Entity(tableName = "flood_alerts")
data class FloodAlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val severity: String, // "ADVISORY", "WARNING", "CRITICAL"
    val affectedBasin: String,
    val message: String,
    val timestamp: Long,
    val isActive: Boolean = true
)
