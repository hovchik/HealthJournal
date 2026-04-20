package com.hovchik.healthjournal.util

import com.hovchik.healthjournal.domain.model.Medication
import com.hovchik.healthjournal.domain.model.Symptom
import com.hovchik.healthjournal.domain.model.VitalSign
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun exportSymptomsCsv(symptoms: List<Symptom>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Name,Intensity,Value,Duration (min),Triggers,Notes")
        for (s in symptoms.sortedByDescending { it.recordedAt }) {
            sb.appendLine(
                "${s.recordedAt.format(dateFormatter)}," +
                "${escapeCsv(s.name)}," +
                "${s.intensity}," +
                "${escapeCsv(s.value ?: "")}," +
                "${s.durationMinutes ?: ""}," +
                "${escapeCsv(s.triggers.joinToString("; "))}," +
                escapeCsv(s.notes)
            )
        }
        return sb.toString()
    }

    fun exportVitalsCsv(vitals: List<VitalSign>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Type,Value,Secondary Value,Unit,Notes")
        for (v in vitals.sortedByDescending { it.recordedAt }) {
            sb.appendLine(
                "${v.recordedAt.format(dateFormatter)}," +
                "${v.type.name}," +
                "${v.value}," +
                "${v.secondaryValue ?: ""}," +
                "${v.type.unit}," +
                escapeCsv(v.notes)
            )
        }
        return sb.toString()
    }

    fun exportMedicationsCsv(medications: List<Medication>): String {
        val sb = StringBuilder()
        sb.appendLine("Name,Dosage,Frequency,Start Date,End Date,Active,Notes")
        for (m in medications) {
            sb.appendLine(
                "${escapeCsv(m.name)}," +
                "${escapeCsv(m.dosage)}," +
                "${escapeCsv(m.frequency)}," +
                "${m.startDate}," +
                "${m.endDate ?: ""}," +
                "${m.active}," +
                escapeCsv(m.notes)
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}
