package com.hovchik.healthjournal.domain.model

enum class InteractionSeverity {
    MINOR,
    MODERATE,
    MAJOR,
    CONTRAINDICATED
}

data class DrugInteraction(
    val drug1: String,
    val drug2: String,
    val severity: InteractionSeverity,
    val description: String
)

data class MedicationAdherence(
    val medicationId: Long,
    val medicationName: String,
    val totalDoses: Int,
    val takenDoses: Int,
    val missedDoses: Int,
    val adherencePercent: Float
)

data class SideEffectEntry(
    val id: Long = 0,
    val medicationId: Long,
    val symptomName: String,
    val severity: Int = 1,
    val notes: String = "",
    val recordedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)
