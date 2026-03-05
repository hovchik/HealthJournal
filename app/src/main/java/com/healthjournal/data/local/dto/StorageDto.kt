package com.healthjournal.data.local.dto

import com.healthjournal.domain.model.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Serializable
data class SymptomDto(
    val id: Long = 0,
    val name: String,
    val intensity: Int,
    val value: String? = null,
    val durationMinutes: Int? = null,
    val triggers: List<String> = emptyList(),
    val notes: String = "",
    val recordedAt: Long = 0,
    val profileId: Long = 0,
    val attachmentPaths: List<String> = emptyList(),
    val diseaseId: Long = 0,
    val isDeleted: Boolean = false
) {
    fun toDomain() = Symptom(
        id = id,
        name = name,
        intensity = intensity,
        value = value,
        durationMinutes = durationMinutes,
        triggers = triggers,
        notes = notes,
        recordedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(recordedAt), ZoneId.systemDefault()),
        profileId = profileId,
        attachmentPaths = attachmentPaths,
        diseaseId = diseaseId,
        isDeleted = isDeleted
    )

    companion object {
        fun from(s: Symptom) = SymptomDto(
            id = s.id,
            name = s.name,
            intensity = s.intensity,
            value = s.value,
            durationMinutes = s.durationMinutes,
            triggers = s.triggers,
            notes = s.notes,
            recordedAt = s.recordedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            profileId = s.profileId,
            attachmentPaths = s.attachmentPaths,
            diseaseId = s.diseaseId,
            isDeleted = s.isDeleted
        )
    }
}

@Serializable
data class VitalSignDto(
    val id: Long = 0,
    val type: String,
    val value: Double,
    val secondaryValue: Double? = null,
    val notes: String = "",
    val recordedAt: Long = 0,
    val profileId: Long = 0,
    val attachmentPaths: List<String> = emptyList(),
    val diseaseId: Long = 0,
    val isDeleted: Boolean = false
) {
    fun toDomain() = VitalSign(
        id = id,
        type = VitalType.valueOf(type),
        value = value,
        secondaryValue = secondaryValue,
        notes = notes,
        recordedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(recordedAt), ZoneId.systemDefault()),
        profileId = profileId,
        attachmentPaths = attachmentPaths,
        diseaseId = diseaseId,
        isDeleted = isDeleted
    )

    companion object {
        fun from(v: VitalSign) = VitalSignDto(
            id = v.id,
            type = v.type.name,
            value = v.value,
            secondaryValue = v.secondaryValue,
            notes = v.notes,
            recordedAt = v.recordedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            profileId = v.profileId,
            attachmentPaths = v.attachmentPaths,
            diseaseId = v.diseaseId,
            isDeleted = v.isDeleted
        )
    }
}

@Serializable
data class MedicationDto(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: Long = 0,
    val endDate: Long? = null,
    val active: Boolean = true,
    val notes: String = "",
    val profileId: Long = 0,
    val diseaseId: Long = 0,
    val isDeleted: Boolean = false
) {
    fun toDomain() = Medication(
        id = id,
        name = name,
        dosage = dosage,
        frequency = frequency,
        startDate = LocalDate.ofEpochDay(startDate),
        endDate = endDate?.let { LocalDate.ofEpochDay(it) },
        active = active,
        notes = notes,
        profileId = profileId,
        diseaseId = diseaseId,
        isDeleted = isDeleted
    )

    companion object {
        fun from(m: Medication) = MedicationDto(
            id = m.id,
            name = m.name,
            dosage = m.dosage,
            frequency = m.frequency,
            startDate = m.startDate.toEpochDay(),
            endDate = m.endDate?.toEpochDay(),
            active = m.active,
            notes = m.notes,
            profileId = m.profileId,
            diseaseId = m.diseaseId,
            isDeleted = m.isDeleted
        )
    }
}

@Serializable
data class MedicationLogDto(
    val id: Long = 0,
    val medicationId: Long,
    val taken: Boolean = true,
    val takenAt: Long = 0,
    val notes: String = ""
) {
    fun toDomain() = MedicationLog(
        id = id,
        medicationId = medicationId,
        taken = taken,
        takenAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(takenAt), ZoneId.systemDefault()),
        notes = notes
    )

    companion object {
        fun from(l: MedicationLog) = MedicationLogDto(
            id = l.id,
            medicationId = l.medicationId,
            taken = l.taken,
            takenAt = l.takenAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            notes = l.notes
        )
    }
}

@Serializable
data class AiReportDto(
    val id: Long = 0,
    val content: String,
    val type: String,
    val periodDays: Int = 7,
    val generatedAt: Long = 0,
    val profileId: Long = 0,
    val diseaseId: Long = 0,
    val providerName: String = "",
    val modelUsed: String = "",
    val prompt: String = ""
) {
    fun toDomain() = AiReport(
        id = id,
        content = content,
        type = ReportType.valueOf(type),
        periodDays = periodDays,
        generatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(generatedAt), ZoneId.systemDefault()),
        profileId = profileId,
        diseaseId = diseaseId,
        providerName = providerName,
        modelUsed = modelUsed,
        prompt = prompt
    )

    companion object {
        fun from(r: AiReport) = AiReportDto(
            id = r.id,
            content = r.content,
            type = r.type.name,
            periodDays = r.periodDays,
            generatedAt = r.generatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            profileId = r.profileId,
            diseaseId = r.diseaseId,
            providerName = r.providerName,
            modelUsed = r.modelUsed,
            prompt = r.prompt
        )
    }
}

@Serializable
data class FamilyMemberDto(
    val id: Long = 0,
    val name: String,
    val relationship: String = "",
    val avatarColor: Int = 0xFF1B6B4D.toInt(),
    val weight: String = "",
    val height: String = "",
    val knownDiseases: List<String> = emptyList()
) {
    fun toDomain() = FamilyMember(
        id = id,
        name = name,
        relationship = relationship,
        avatarColor = avatarColor,
        weight = weight,
        height = height,
        knownDiseases = knownDiseases
    )

    companion object {
        fun from(f: FamilyMember) = FamilyMemberDto(
            id = f.id,
            name = f.name,
            relationship = f.relationship,
            avatarColor = f.avatarColor,
            weight = f.weight,
            height = f.height,
            knownDiseases = f.knownDiseases
        )
    }
}

@Serializable
data class DiseaseDto(
    val id: Long = 0,
    val name: String,
    val profileId: Long = 0,
    val notes: String = "",
    val active: Boolean = true,
    val createdAt: Long = 0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val group: String = ""
) {
    fun toDomain() = Disease(
        id = id,
        name = name,
        profileId = profileId,
        notes = notes,
        active = active,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        isDeleted = isDeleted,
        deletedAt = deletedAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        group = group
    )

    companion object {
        fun from(d: Disease) = DiseaseDto(
            id = d.id,
            name = d.name,
            profileId = d.profileId,
            notes = d.notes,
            active = d.active,
            createdAt = d.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            isDeleted = d.isDeleted,
            deletedAt = d.deletedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            group = d.group
        )
    }
}
