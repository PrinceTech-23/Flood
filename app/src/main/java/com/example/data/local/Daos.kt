package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorStationDao {
    @Query("SELECT * FROM sensor_stations ORDER BY currentWaterLevelM / dangerLevelM DESC")
    fun getAllStations(): Flow<List<SensorStationEntity>>

    @Query("SELECT * FROM sensor_stations WHERE id = :stationId")
    fun getStationById(stationId: String): Flow<SensorStationEntity?>

    @Query("SELECT * FROM sensor_stations WHERE id = :stationId")
    suspend fun getStationDirect(stationId: String): SensorStationEntity?

    @Query("SELECT * FROM sensor_stations WHERE isFavorite = 1")
    fun getFavoriteStations(): Flow<List<SensorStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<SensorStationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: SensorStationEntity)

    @Update
    suspend fun updateStation(station: SensorStationEntity)

    @Query("UPDATE sensor_stations SET isFavorite = :isFav WHERE id = :stationId")
    suspend fun setFavorite(stationId: String, isFav: Boolean)

    @Query("DELETE FROM sensor_stations WHERE id = :stationId")
    suspend fun deleteStation(stationId: String)
}

@Dao
interface TelemetryReadingDao {
    @Query("SELECT * FROM telemetry_readings WHERE stationId = :stationId ORDER BY timestamp ASC")
    fun getReadingsForStation(stationId: String): Flow<List<TelemetryReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<TelemetryReadingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: TelemetryReadingEntity)

    @Query("DELETE FROM telemetry_readings WHERE stationId = :stationId")
    suspend fun clearReadingsForStation(stationId: String)
}

@Dao
interface CitizenReportDao {
    @Query("SELECT * FROM citizen_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<CitizenReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CitizenReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<CitizenReportEntity>)

    @Query("UPDATE citizen_reports SET verified = 1 WHERE id = :reportId")
    suspend fun verifyReport(reportId: String)
}

@Dao
interface FloodAlertDao {
    @Query("SELECT * FROM flood_alerts WHERE isActive = 1 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<FloodAlertEntity>>

    @Query("SELECT * FROM flood_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<FloodAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<FloodAlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: FloodAlertEntity)

    @Query("UPDATE flood_alerts SET isActive = 0 WHERE id = :alertId")
    suspend fun dismissAlert(alertId: String)
}
