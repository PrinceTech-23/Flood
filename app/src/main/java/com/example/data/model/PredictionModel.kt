package com.example.data.model

data class InundationForecast(
    val stationId: String,
    val stationName: String,
    val currentLevelM: Double,
    val predictedPeakLevelM: Double,
    val crestLimitM: Double,
    val hoursToPeak: Double,
    val overflowProbabilityPercent: Int,
    val riskRating: String, // "LOW", "MODERATE", "HIGH", "EXTREME"
    val estimatedRunoffCubicMetersPerSec: Double,
    val hourlyForecastCurve: List<ForecastHourPoint>,
    val mitigationAction: String
)

data class ForecastHourPoint(
    val hourOffset: Int,
    val waterLevelM: Double,
    val precipitationMm: Double
)

object HydrologyPredictionEngine {
    fun calculateForecast(
        stationName: String,
        stationId: String,
        currentWaterLevelM: Double,
        warningLevelM: Double,
        dangerLevelM: Double,
        crestLimitM: Double,
        rainfallMm1h: Double,
        rainfallMm24h: Double,
        rateOfRiseCmHr: Double
    ): InundationForecast {
        // Hydrological calculation based on current rate of rise and rainfall runoff momentum
        val rainRunoffFactor = (rainfallMm1h * 0.025) + (rainfallMm24h * 0.005)
        val riseMomentum = (rateOfRiseCmHr / 100.0) * 2.5
        val peakDelta = (rainRunoffFactor + riseMomentum).coerceAtLeast(0.15)
        val predictedPeak = currentWaterLevelM + peakDelta
        val hoursToPeak = if (rateOfRiseCmHr > 0) {
            ((peakDelta / (rateOfRiseCmHr / 100.0)).coerceIn(1.5, 9.0))
        } else {
            3.0
        }

        val overflowMargin = crestLimitM - predictedPeak
        val overflowProbability = when {
            overflowMargin <= 0 -> 98
            overflowMargin < 0.3 -> 85
            overflowMargin < 0.6 -> 60
            overflowMargin < 1.0 -> 35
            else -> 12
        }

        val riskRating = when {
            overflowProbability >= 75 || predictedPeak >= dangerLevelM -> "EXTREME"
            overflowProbability >= 45 || predictedPeak >= warningLevelM -> "HIGH"
            overflowProbability >= 25 -> "MODERATE"
            else -> "LOW"
        }

        val points = mutableListOf<ForecastHourPoint>()
        for (h in 0..12) {
            val progress = (h.toDouble() / hoursToPeak).coerceIn(0.0, 2.0)
            val level = if (progress <= 1.0) {
                currentWaterLevelM + (peakDelta * Math.sin(progress * Math.PI / 2))
            } else {
                predictedPeak - ((progress - 1.0) * peakDelta * 0.35)
            }
            val precip = (rainfallMm1h * Math.exp(-h * 0.18)).coerceAtLeast(0.0)
            points.add(
                ForecastHourPoint(
                    hourOffset = h,
                    waterLevelM = Math.round(level * 100.0) / 100.0,
                    precipitationMm = Math.round(precip * 10.0) / 10.0
                )
            )
        }

        val action = when (riskRating) {
            "EXTREME" -> "Deploy flood gates, issue mandatory evacuation orders for Zone A, activate municipal retention pumps."
            "HIGH" -> "Place sandbags around culverts, pre-position emergency response boats, advise citizens in basements to relocate."
            "MODERATE" -> "Monitor Doppler radar stations closely, clear storm drains of tree branches and urban silt."
            else -> "System stable. Continue routine 15-minute telemetry polling."
        }

        return InundationForecast(
            stationId = stationId,
            stationName = stationName,
            currentLevelM = currentWaterLevelM,
            predictedPeakLevelM = Math.round(predictedPeak * 100.0) / 100.0,
            crestLimitM = crestLimitM,
            hoursToPeak = Math.round(hoursToPeak * 10.0) / 10.0,
            overflowProbabilityPercent = overflowProbability,
            riskRating = riskRating,
            estimatedRunoffCubicMetersPerSec = Math.round((currentWaterLevelM * 14.5 + rainfallMm24h * 2.1) * 10.0) / 10.0,
            hourlyForecastCurve = points,
            mitigationAction = action
        )
    }
}
