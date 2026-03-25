package com.healthjournal.util

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class HealthConnectManager(private val context: Context) {

    private val client: HealthConnectClient? by lazy {
        try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else null
        } catch (e: Exception) {
            Log.w("HealthConnect", "Health Connect not available", e)
            null
        }
    }

    val isAvailable: Boolean get() = client != null

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
    )

    suspend fun hasAllPermissions(): Boolean {
        val granted = client?.permissionController?.getGrantedPermissions() ?: return false
        return requiredPermissions.all { it in granted }
    }

    suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<VitalSign> {
        val hc = client ?: return emptyList()
        return try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.flatMap { record ->
                record.samples.map { sample ->
                    VitalSign(
                        type = VitalType.PULSE,
                        value = sample.beatsPerMinute.toDouble(),
                        recordedAt = LocalDateTime.ofInstant(sample.time, ZoneId.systemDefault())
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading heart rate", e)
            emptyList()
        }
    }

    suspend fun readSteps(startTime: Instant, endTime: Instant): List<VitalSign> {
        val hc = client ?: return emptyList()
        return try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { record ->
                VitalSign(
                    type = VitalType.STEPS,
                    value = record.count.toDouble(),
                    recordedAt = LocalDateTime.ofInstant(record.endTime, ZoneId.systemDefault())
                )
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading steps", e)
            emptyList()
        }
    }

    suspend fun readSleep(startTime: Instant, endTime: Instant): List<VitalSign> {
        val hc = client ?: return emptyList()
        return try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { record ->
                val durationHours = java.time.Duration.between(record.startTime, record.endTime).toMinutes() / 60.0
                VitalSign(
                    type = VitalType.SLEEP,
                    value = durationHours,
                    recordedAt = LocalDateTime.ofInstant(record.endTime, ZoneId.systemDefault())
                )
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading sleep", e)
            emptyList()
        }
    }

    suspend fun readSpO2(startTime: Instant, endTime: Instant): List<VitalSign> {
        val hc = client ?: return emptyList()
        return try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { record ->
                VitalSign(
                    type = VitalType.SPO2,
                    value = record.percentage.value,
                    recordedAt = LocalDateTime.ofInstant(record.time, ZoneId.systemDefault())
                )
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading SpO2", e)
            emptyList()
        }
    }

    suspend fun readBloodPressure(startTime: Instant, endTime: Instant): List<VitalSign> {
        val hc = client ?: return emptyList()
        return try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { record ->
                VitalSign(
                    type = VitalType.BLOOD_PRESSURE,
                    value = record.systolic.inMillimetersOfMercury,
                    secondaryValue = record.diastolic.inMillimetersOfMercury,
                    recordedAt = LocalDateTime.ofInstant(record.time, ZoneId.systemDefault())
                )
            }
        } catch (e: Exception) {
            Log.e("HealthConnect", "Error reading blood pressure", e)
            emptyList()
        }
    }

    suspend fun importAllRecent(profileId: Long): List<VitalSign> {
        val endTime = Instant.now()
        val startTime = endTime.minus(java.time.Duration.ofDays(7))

        val vitals = mutableListOf<VitalSign>()
        vitals.addAll(readHeartRate(startTime, endTime))
        vitals.addAll(readSteps(startTime, endTime))
        vitals.addAll(readSleep(startTime, endTime))
        vitals.addAll(readSpO2(startTime, endTime))
        vitals.addAll(readBloodPressure(startTime, endTime))

        return vitals.map { it.copy(profileId = profileId) }
    }
}
