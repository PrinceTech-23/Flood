package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SensorStationEntity::class,
        TelemetryReadingEntity::class,
        CitizenReportEntity::class,
        FloodAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FloodDatabase : RoomDatabase() {
    abstract fun sensorStationDao(): SensorStationDao
    abstract fun telemetryReadingDao(): TelemetryReadingDao
    abstract fun citizenReportDao(): CitizenReportDao
    abstract fun floodAlertDao(): FloodAlertDao

    companion object {
        @Volatile
        private var INSTANCE: FloodDatabase? = null

        fun getDatabase(context: Context): FloodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloodDatabase::class.java,
                    "floodwatch_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: FloodDatabase) {
            val now = System.currentTimeMillis()
            val stationDao = database.sensorStationDao()
            val telemetryDao = database.telemetryReadingDao()
            val reportDao = database.citizenReportDao()
            val alertDao = database.floodAlertDao()

            val seedStations = listOf(
                SensorStationEntity(
                    id = "STN-ESP32-01",
                    name = "Cedar River North Dam",
                    basinName = "Cedar River Basin",
                    latitude = 41.9779,
                    longitude = -91.6656,
                    currentWaterLevelM = 5.82,
                    warningLevelM = 4.80,
                    dangerLevelM = 5.50,
                    crestLimitM = 6.40,
                    rainfallMm1h = 24.5,
                    rainfallMm24h = 89.2,
                    rateOfRiseCmHr = 18.5,
                    hardwareType = "ESP32-S3 + JSN-SR04T Dual Ultrasonic",
                    transmissionProtocol = "LoRa 868MHz (Gateway GW-04)",
                    batteryPercent = 94,
                    signalDbm = -68,
                    lastReportedTime = now - 90_000,
                    status = "CRITICAL",
                    isFavorite = true
                ),
                SensorStationEntity(
                    id = "STN-ESP32-02",
                    name = "Valley View Culvert & Weir",
                    basinName = "Cedar River Basin",
                    latitude = 41.9612,
                    longitude = -91.6421,
                    currentWaterLevelM = 4.10,
                    warningLevelM = 3.80,
                    dangerLevelM = 4.60,
                    crestLimitM = 5.20,
                    rainfallMm1h = 16.2,
                    rainfallMm24h = 62.0,
                    rateOfRiseCmHr = 11.2,
                    hardwareType = "NodeMCU ESP8266 + Submersible Hydrostatic",
                    transmissionProtocol = "GSM 4G NB-IoT (SIM7000G)",
                    batteryPercent = 82,
                    signalDbm = -79,
                    lastReportedTime = now - 180_000,
                    status = "WARNING",
                    isFavorite = true
                ),
                SensorStationEntity(
                    id = "STN-ESP32-03",
                    name = "Eastside Retention Basin A",
                    basinName = "Urban Metro Drainage",
                    latitude = 41.9890,
                    longitude = -91.6110,
                    currentWaterLevelM = 2.95,
                    warningLevelM = 3.20,
                    dangerLevelM = 4.00,
                    crestLimitM = 4.80,
                    rainfallMm1h = 9.8,
                    rainfallMm24h = 44.5,
                    rateOfRiseCmHr = 5.0,
                    hardwareType = "ESP32-WROOM-32 + HC-SR04 Sealed",
                    transmissionProtocol = "MQTT via Wi-Fi Mesh",
                    batteryPercent = 97,
                    signalDbm = -55,
                    lastReportedTime = now - 45_000,
                    status = "ADVISORY",
                    isFavorite = false
                ),
                SensorStationEntity(
                    id = "STN-ESP32-04",
                    name = "Industrial Park Spillway",
                    basinName = "Lower Creek Corridor",
                    latitude = 41.9440,
                    longitude = -91.6850,
                    currentWaterLevelM = 1.45,
                    warningLevelM = 2.60,
                    dangerLevelM = 3.50,
                    crestLimitM = 4.20,
                    rainfallMm1h = 3.2,
                    rainfallMm24h = 22.0,
                    rateOfRiseCmHr = 0.8,
                    hardwareType = "STM32L4 + Radar Gauge 24GHz",
                    transmissionProtocol = "LoRaWAN EU868",
                    batteryPercent = 88,
                    signalDbm = -82,
                    lastReportedTime = now - 300_000,
                    status = "NORMAL",
                    isFavorite = false
                ),
                SensorStationEntity(
                    id = "STN-ESP32-05",
                    name = "Black Hawk Creek Confluence",
                    basinName = "Cedar River Basin",
                    latitude = 42.0120,
                    longitude = -91.7100,
                    currentWaterLevelM = 4.90,
                    warningLevelM = 4.50,
                    dangerLevelM = 5.20,
                    crestLimitM = 6.00,
                    rainfallMm1h = 21.0,
                    rainfallMm24h = 78.4,
                    rateOfRiseCmHr = 15.6,
                    hardwareType = "ESP32-S3 + Doppler Flow Meter",
                    transmissionProtocol = "GSM 4G NB-IoT",
                    batteryPercent = 76,
                    signalDbm = -89,
                    lastReportedTime = now - 120_000,
                    status = "WARNING",
                    isFavorite = false
                )
            )
            stationDao.insertStations(seedStations)

            // Seed telemetry readings for the last 12 hours
            val readingsList = mutableListOf<TelemetryReadingEntity>()
            seedStations.forEach { station ->
                val baseLevel = station.currentWaterLevelM
                for (i in 12 downTo 0) {
                    val timestamp = now - (i * 3600_000L)
                    // Trend upwards for critical stations
                    val offset = if (station.status == "CRITICAL" || station.status == "WARNING") {
                        (12 - i) * 0.12 - 0.7
                    } else {
                        (12 - i) * 0.03 - 0.15
                    }
                    val level = (baseLevel + offset).coerceAtLeast(0.5)
                    readingsList.add(
                        TelemetryReadingEntity(
                            stationId = station.id,
                            timestamp = timestamp,
                            waterLevelM = Math.round(level * 100.0) / 100.0,
                            rainfallMm = (station.rainfallMm1h * (1.0 - (i % 3) * 0.2)).coerceAtLeast(0.0),
                            flowVelocityMs = (1.2 + (level / station.crestLimitM) * 1.8)
                        )
                    )
                }
            }
            telemetryDao.insertReadings(readingsList)

            // Seed active flood alerts
            val seedAlerts = listOf(
                FloodAlertEntity(
                    id = "ALT-001",
                    title = "FLASH FLOOD WARNING: Cedar River North Dam",
                    severity = "CRITICAL",
                    affectedBasin = "Cedar River Basin",
                    message = "Ultrasonic sensors detect water level has breached 5.82m (Threshold 5.50m). Overflow predicted within 45 minutes. Evacuation advised for low-lying zones A & B.",
                    timestamp = now - 1800_000,
                    isActive = true
                ),
                FloodAlertEntity(
                    id = "ALT-002",
                    title = "CULVERT SURGE ADVISORY: Valley View Area",
                    severity = "WARNING",
                    affectedBasin = "Cedar River Basin",
                    message = "Submersible hydrostatic gauge indicates rapid surge (+11.2 cm/hr). High risk of roadway pooling on Route 30 underpass.",
                    timestamp = now - 5400_000,
                    isActive = true
                )
            )
            alertDao.insertAlerts(seedAlerts)

            // Seed citizen reports
            val seedReports = listOf(
                CitizenReportEntity(
                    id = "REP-01",
                    title = "Riverwalk Trail Underpass Inundated",
                    locationName = "Cedar River East Greenway",
                    latitude = 41.9740,
                    longitude = -91.6620,
                    waterDepthCategory = "Waist (1m+)",
                    hazardType = "River Breach",
                    description = "River has spilled onto pedestrian bridge access. Strong current, completely impassable.",
                    timestamp = now - 3600_000,
                    verified = true,
                    reporterName = "Marcus Vance (Civil Defense Volunteer)"
                ),
                CitizenReportEntity(
                    id = "REP-02",
                    title = "Drainage Grate Clogged by Debris",
                    locationName = "Oakridge Avenue & 14th St",
                    latitude = 41.9820,
                    longitude = -91.6350,
                    waterDepthCategory = "Knee (30-50cm)",
                    hazardType = "Blocked Culvert",
                    description = "Heavy runoff trapped behind fallen branches. Water encroaching front yards.",
                    timestamp = now - 7200_000,
                    verified = false,
                    reporterName = "Elena Rostova (Resident)"
                )
            )
            reportDao.insertReports(seedReports)
        }
    }
}
