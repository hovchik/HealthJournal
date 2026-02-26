package com.healthjournal.data.repository

import com.healthjournal.data.local.entity.*
import com.healthjournal.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val json = Json { ignoreUnknownKeys = true }

// Symptom
fun SymptomEntity.toDomain() = Symptom(
    id = id,
    name = name,
    intensity = intensity,
    durationMinutes = durationMinutes,
    triggers = if (triggers.isBlank()) emptyList() else json.decodeFromString(triggers),
    notes = notes,
    recordedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(recordedAt), ZoneId.systemDefault())
)

fun Symptom.toEntity() = SymptomEntity(
    id = id,
    name = name,
    intensity = intensity,
    durationMinutes = durationMinutes,
    triggers = json.encodeToString(triggers),
    notes = notes,
    recordedAt = recordedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

// VitalSign
fun VitalSignEntity.toDomain() = VitalSign(
    id = id,
    type = VitalType.valueOf(type),
    value = value,
    secondaryValue = secondaryValue,
    notes = notes,
    recordedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(recordedAt), ZoneId.systemDefault())
)

fun VitalSign.toEntity() = VitalSignEntity(
    id = id,
    type = type.name,
    value = value,
    secondaryValue = secondaryValue,
    notes = notes,
    recordedAt = recordedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

// Medication
fun MedicationEntity.toDomain() = Medication(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    startDate = LocalDate.ofEpochDay(startDate),
    endDate = endDate?.let { LocalDate.ofEpochDay(it) },
    active = active,
    notes = notes
)

fun Medication.toEntity() = MedicationEntity(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    startDate = startDate.toEpochDay(),
    endDate = endDate?.toEpochDay(),
    active = active,
    notes = notes
)

// MedicationLog
fun MedicationLogEntity.toDomain() = MedicationLog(
    id = id,
    medicationId = medicationId,
    taken = taken,
    takenAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(takenAt), ZoneId.systemDefault()),
    notes = notes
)

fun MedicationLog.toEntity() = MedicationLogEntity(
    id = id,
    medicationId = medicationId,
    taken = taken,
    takenAt = takenAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    notes = notes
)

// AiReport
fun AiReportEntity.toDomain() = AiReport(
    id = id,
    content = content,
    type = ReportType.valueOf(type),
    periodDays = periodDays,
    generatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(generatedAt), ZoneId.systemDefault())
)

fun AiReport.toEntity() = AiReportEntity(
    id = id,
    content = content,
    type = type.name,
    periodDays = periodDays,
    generatedAt = generatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
