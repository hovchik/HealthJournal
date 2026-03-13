package com.healthjournal.data.ai

import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.model.ai.*
import java.time.format.DateTimeFormatter

/**
 * Reusable local rule-based analysis engine for health data.
 * Provides structured summaries and pattern analysis without requiring AI inference.
 */
object LocalAnalysisEngine {

    enum class TrendDirection { UP, DOWN, STABLE }

    fun detectTrend(values: List<Double>): TrendDirection {
        if (values.size < 2) return TrendDirection.STABLE
        val n = values.size
        val firstHalf = values.take(n / 2 + n % 2).average()
        val secondHalf = values.drop(n / 2 + n % 2).average()
        val avg = values.average()
        val threshold = if (avg != 0.0) avg * 0.05 else 1.0
        return when {
            secondHalf - firstHalf > threshold -> TrendDirection.UP
            firstHalf - secondHalf > threshold -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
    }

    fun formatVitalValue(value: Double, secondary: Double?, type: VitalType): String =
        if (type == VitalType.BLOOD_PRESSURE && secondary != null) {
            "${value.toInt()}/${secondary.toInt()}"
        } else if (type == VitalType.TEMPERATURE || type == VitalType.GLUCOSE) {
            "%.1f".format(value)
        } else {
            value.toInt().toString()
        }

    fun formatValue(value: Double, type: VitalType): String =
        if (type == VitalType.TEMPERATURE || type == VitalType.GLUCOSE) {
            "%.1f".format(value)
        } else {
            value.toInt().toString()
        }

    internal fun checkVitalRange(value: Double, secondary: Double?, type: VitalType, t: AnalysisTexts): String {
        return when (type) {
            VitalType.BLOOD_PRESSURE -> {
                val flags = mutableListOf<String>()
                if (value >= 140) flags.add(t.highBP)
                else if (value < 90) flags.add(t.lowBP)
                if (secondary != null) {
                    if (secondary >= 90) flags.add(t.highDiastolic)
                    else if (secondary < 60) flags.add(t.lowDiastolic)
                }
                flags.joinToString(", ")
            }
            VitalType.PULSE -> when {
                value > 100 -> t.highPulse
                value < 60 -> t.lowPulse
                else -> ""
            }
            VitalType.TEMPERATURE -> when {
                value >= 38.0 -> t.fever
                value >= 37.5 -> t.subfebrile
                value < 35.5 -> t.hypothermia
                else -> ""
            }
            VitalType.SPO2 -> when {
                value < 90 -> t.criticalO2
                value < 95 -> t.lowO2
                else -> ""
            }
            VitalType.GLUCOSE -> when {
                value > 11.0 -> t.highGlucose
                value > 7.8 -> t.elevatedGlucose
                value < 3.9 -> t.lowGlucose
                else -> ""
            }
            else -> ""
        }
    }

    internal fun generateObservations(input: AiInput, t: AnalysisTexts): List<String> {
        val obs = mutableListOf<String>()

        val severe = input.symptoms.filter { it.intensity >= 7 }
        if (severe.isNotEmpty()) {
            val names = severe.map { it.name }.distinct().joinToString(", ")
            obs.add("${t.severeSymptoms}: $names")
        }

        val symptomGroups = input.symptoms.groupBy { it.name }
        symptomGroups.filter { it.value.size >= 3 }.forEach { (name, occurrences) ->
            obs.add("${t.recurringSymptom}: $name (${occurrences.size}x)")
        }

        val abnormalVitals = mutableSetOf<String>()
        input.vitals.forEach { v ->
            val flag = checkVitalRange(v.value, v.secondaryValue, v.type, t)
            if (flag.isNotEmpty()) {
                val valueStr = formatVitalValue(v.value, v.secondaryValue, v.type)
                val key = "${v.type.displayName} $valueStr"
                if (abnormalVitals.add(key)) {
                    obs.add("${v.type.displayName} $valueStr ${v.type.unit}: $flag")
                }
            }
        }

        if (input.knownDiseases.isNotEmpty()) {
            val diseases = input.knownDiseases.map { it.lowercase() }
            val isDiabetes = diseases.any {
                it.contains("\u0434\u0438\u0430\u0431\u0435\u0442") || it.contains("diabetes") ||
                    it.contains("\u7CD6\u5C3F\u75C5") || it.contains("diabet")
            }
            if (isDiabetes) {
                val highGlucose = input.vitals.filter { it.type == VitalType.GLUCOSE && it.value > 7.8 }
                if (highGlucose.isNotEmpty()) {
                    obs.add(t.diabetesGlucoseWarning)
                }
            }
            val isHypertension = diseases.any {
                it.contains("\u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D") || it.contains("hypertens") ||
                    it.contains("\u9AD8\u8840\u538B") || it.contains("hipertens")
            }
            if (isHypertension) {
                val highBp = input.vitals.filter { it.type == VitalType.BLOOD_PRESSURE && it.value >= 140 }
                if (highBp.isNotEmpty()) {
                    obs.add(t.hypertensionBPWarning)
                }
            }
        }

        return obs
    }

    internal fun findCorrelations(input: AiInput, t: AnalysisTexts): List<String> {
        val correlations = mutableListOf<String>()
        if (input.symptoms.isEmpty() || input.vitals.isEmpty()) return correlations

        val symptomDays = input.symptoms.groupBy { it.recordedAt.toLocalDate() }
        val vitalDays = input.vitals.groupBy { it.recordedAt.toLocalDate() }

        for ((date, symptoms) in symptomDays) {
            val vitalsOnDay = vitalDays[date] ?: continue
            val abnormal = vitalsOnDay.filter { v ->
                checkVitalRange(v.value, v.secondaryValue, v.type, t).isNotEmpty()
            }
            if (abnormal.isNotEmpty()) {
                val symptomNames = symptoms.map { it.name }.distinct().joinToString(", ")
                val vitalNames = abnormal.map { it.type.displayName }.distinct().joinToString(", ")
                correlations.add("${t.sameDayCorrelation}: $symptomNames + $vitalNames ($date)")
            }
        }

        return correlations.distinct().take(5)
    }

    // ---- Local summary ----

    fun buildLocalSummary(input: AiInput): String {
        val t = AnalysisTexts.forLanguage(input.outputLanguage)
        val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return buildString {
            appendLine(t.summaryTitle)
            appendLine(t.localAnalysisNote)
            appendLine()

            // Patient info
            val hasPatientInfo = input.weight.isNotBlank() || input.height.isNotBlank() ||
                input.age.isNotBlank() || input.gender.isNotBlank() ||
                input.knownDiseases.isNotEmpty() || input.diseaseName.isNotBlank()
            if (hasPatientInfo) {
                appendLine("=== ${t.patientInfo} ===")
                if (input.age.isNotBlank()) appendLine("${t.ageLabel}: ${input.age}")
                if (input.gender.isNotBlank()) appendLine("${t.genderLabel}: ${input.gender}")
                if (input.weight.isNotBlank()) appendLine("${t.weightLabel}: ${input.weight}")
                if (input.height.isNotBlank()) appendLine("${t.heightLabel}: ${input.height}")
                if (input.knownDiseases.isNotEmpty()) {
                    appendLine("${t.knownConditions}: ${input.knownDiseases.joinToString(", ")}")
                }
                if (input.diseaseName.isNotBlank()) {
                    appendLine("${t.analyzingDisease}: ${input.diseaseName}")
                }
                appendLine()
            }

            // Symptoms
            appendLine("=== ${t.symptomsSection} ===")
            if (input.symptoms.isEmpty()) {
                appendLine(t.noSymptoms)
            } else {
                input.symptoms.sortedByDescending { it.recordedAt }.forEach { s ->
                    append("\u2022 ${s.name} \u2014 ${t.intensity}: ${s.intensity}/10")
                    appendLine(" (${s.recordedAt.format(dateFmt)})")
                    if (s.triggers.isNotEmpty()) appendLine("  ${t.triggers}: ${s.triggers.joinToString(", ")}")
                    if (s.notes.isNotBlank()) appendLine("  ${t.notes}: ${s.notes}")
                }
            }
            appendLine()

            // Vitals
            appendLine("=== ${t.vitalsSection} ===")
            if (input.vitals.isEmpty()) {
                appendLine(t.noVitals)
            } else {
                val grouped = input.vitals.groupBy { it.type }
                for ((type, vitals) in grouped) {
                    val sorted = vitals.sortedByDescending { it.recordedAt }
                    appendLine("\u25B8 ${type.displayName}:")
                    sorted.forEach { v ->
                        val valueStr = formatVitalValue(v.value, v.secondaryValue, type)
                        val flag = checkVitalRange(v.value, v.secondaryValue, type, t)
                        val flagStr = if (flag.isNotEmpty()) " \u26A0 $flag" else ""
                        appendLine("  ${v.recordedAt.format(dateFmt)}: $valueStr ${type.unit}$flagStr")
                    }
                    if (sorted.size >= 2) {
                        val trend = detectTrend(sorted.reversed().map { it.value })
                        val trendLabel = when (trend) {
                            TrendDirection.UP -> t.trendUp
                            TrendDirection.DOWN -> t.trendDown
                            TrendDirection.STABLE -> t.trendStable
                        }
                        appendLine("  ${t.trend}: $trendLabel")
                    }
                    appendLine()
                }
            }

            // Medications
            appendLine("=== ${t.medicationsSection} ===")
            if (input.medications.isEmpty()) {
                appendLine(t.noMedications)
            } else {
                input.medications.forEach { m ->
                    appendLine("\u2022 ${m.name} \u2014 ${m.dosage}, ${m.frequency}")
                }
            }
            appendLine()

            // Observations
            val observations = generateObservations(input, t)
            if (observations.isNotEmpty()) {
                appendLine("=== ${t.observationsSection} ===")
                observations.forEach { appendLine("\u2022 $it") }
                appendLine()
            }

            appendLine(t.disclaimer)
        }
    }

    // ---- Local pattern analysis ----

    fun buildLocalPatternAnalysis(input: AiInput): String {
        val t = AnalysisTexts.forLanguage(input.outputLanguage)
        val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return buildString {
            appendLine(t.patternTitle)
            appendLine(t.localAnalysisNote)
            appendLine()

            // Vital trends
            val vitalGroups = input.vitals.groupBy { it.type }
            if (vitalGroups.any { it.value.size >= 2 }) {
                appendLine("=== ${t.vitalTrends} ===")
                for ((type, vitals) in vitalGroups) {
                    val sorted = vitals.sortedBy { it.recordedAt }
                    if (sorted.size < 2) continue

                    val values = sorted.map { it.value }
                    val trend = detectTrend(values)
                    val min = values.min()
                    val max = values.max()
                    val avg = values.average()

                    val trendLabel = when (trend) {
                        TrendDirection.UP -> "\u2191 ${t.trendUp}"
                        TrendDirection.DOWN -> "\u2193 ${t.trendDown}"
                        TrendDirection.STABLE -> "\u2192 ${t.trendStable}"
                    }

                    appendLine("\u25B8 ${type.displayName}: $trendLabel")
                    appendLine("  ${t.minValue}: ${formatValue(min, type)} | " +
                        "${t.maxValue}: ${formatValue(max, type)} | " +
                        "${t.avgValue}: ${formatValue(avg, type)} ${type.unit}")

                    if (type == VitalType.BLOOD_PRESSURE) {
                        val diastolicValues = sorted.mapNotNull { it.secondaryValue }
                        if (diastolicValues.size >= 2) {
                            val dTrend = detectTrend(diastolicValues)
                            val dLabel = when (dTrend) {
                                TrendDirection.UP -> "\u2191 ${t.trendUp}"
                                TrendDirection.DOWN -> "\u2193 ${t.trendDown}"
                                TrendDirection.STABLE -> "\u2192 ${t.trendStable}"
                            }
                            appendLine("  ${t.diastolicLabel}: $dLabel " +
                                "(${diastolicValues.min().toInt()}-${diastolicValues.max().toInt()} ${type.unit})")
                        }
                    }

                    val abnormal = sorted.filter { v ->
                        checkVitalRange(v.value, v.secondaryValue, type, t).isNotEmpty()
                    }
                    if (abnormal.isNotEmpty()) {
                        appendLine("  \u26A0 ${t.abnormalReadings}: ${abnormal.size}/${sorted.size}")
                    }
                    appendLine()
                }
            }

            // Symptom frequency
            if (input.symptoms.isNotEmpty()) {
                appendLine("=== ${t.symptomFrequency} ===")
                val symptomGroups = input.symptoms.groupBy { it.name }
                val sortedGroups = symptomGroups.entries.sortedByDescending { it.value.size }

                for ((name, occurrences) in sortedGroups) {
                    val count = occurrences.size
                    val avgIntensity = occurrences.map { it.intensity }.average()
                    val maxIntensity = occurrences.maxOf { it.intensity }

                    appendLine("\u25B8 $name: ${count}x")
                    appendLine("  ${t.avgIntensity}: ${"%.1f".format(avgIntensity)}/10, ${t.maxIntensity}: $maxIntensity/10")

                    val allTriggers = occurrences.flatMap { it.triggers }
                    if (allTriggers.isNotEmpty()) {
                        val triggerCounts = allTriggers.groupingBy { it }.eachCount()
                            .entries.sortedByDescending { it.value }
                        appendLine("  ${t.commonTriggers}: ${triggerCounts.joinToString(", ") { "${it.key} (${it.value}x)" }}")
                    }

                    if (count >= 2) {
                        val dates = occurrences.sortedBy { it.recordedAt }
                        appendLine("  ${t.period}: ${dates.first().recordedAt.format(dateFmt)} \u2014 ${dates.last().recordedAt.format(dateFmt)}")
                    }
                    appendLine()
                }
            }

            // Correlations
            val correlations = findCorrelations(input, t)
            if (correlations.isNotEmpty()) {
                appendLine("=== ${t.correlationsSection} ===")
                correlations.forEach { appendLine("\u2022 $it") }
                appendLine()
            }

            if (input.symptoms.isEmpty() && input.vitals.isEmpty()) {
                appendLine(t.noPatterns)
                appendLine()
            }

            appendLine(t.disclaimer)
        }
    }
}

// ==================== Localized analysis texts ====================

internal data class AnalysisTexts(
    // Headers
    val summaryTitle: String,
    val patternTitle: String,
    val localAnalysisNote: String,
    val patientInfo: String,
    val symptomsSection: String,
    val vitalsSection: String,
    val medicationsSection: String,
    val observationsSection: String,
    val correlationsSection: String,
    val vitalTrends: String,
    val symptomFrequency: String,
    // Labels
    val ageLabel: String,
    val genderLabel: String,
    val weightLabel: String,
    val heightLabel: String,
    val knownConditions: String,
    val analyzingDisease: String,
    val intensity: String,
    val triggers: String,
    val notes: String,
    val trend: String,
    val trendUp: String,
    val trendDown: String,
    val trendStable: String,
    val minValue: String,
    val maxValue: String,
    val avgValue: String,
    val avgIntensity: String,
    val maxIntensity: String,
    val commonTriggers: String,
    val abnormalReadings: String,
    val period: String,
    val diastolicLabel: String,
    // Empty states
    val noSymptoms: String,
    val noVitals: String,
    val noMedications: String,
    val noPatterns: String,
    // Vital range flags
    val highBP: String,
    val lowBP: String,
    val highDiastolic: String,
    val lowDiastolic: String,
    val highPulse: String,
    val lowPulse: String,
    val fever: String,
    val subfebrile: String,
    val hypothermia: String,
    val criticalO2: String,
    val lowO2: String,
    val highGlucose: String,
    val elevatedGlucose: String,
    val lowGlucose: String,
    // Observations
    val severeSymptoms: String,
    val recurringSymptom: String,
    val sameDayCorrelation: String,
    val diabetesGlucoseWarning: String,
    val hypertensionBPWarning: String,
    val disclaimer: String
) {
    companion object {
        val EN = AnalysisTexts(
            summaryTitle = "[Health Data Summary]",
            patternTitle = "[Pattern Analysis]",
            localAnalysisNote = "Analyzed locally on your device. Your data stays private.",
            patientInfo = "PATIENT INFO",
            symptomsSection = "SYMPTOMS",
            vitalsSection = "VITAL SIGNS",
            medicationsSection = "MEDICATIONS",
            observationsSection = "OBSERVATIONS",
            correlationsSection = "CORRELATIONS",
            vitalTrends = "VITAL SIGN TRENDS",
            symptomFrequency = "SYMPTOM FREQUENCY",
            ageLabel = "Age",
            genderLabel = "Gender",
            weightLabel = "Weight",
            heightLabel = "Height",
            knownConditions = "Known conditions",
            analyzingDisease = "Analyzing disease",
            intensity = "intensity",
            triggers = "Triggers",
            notes = "Notes",
            trend = "Trend",
            trendUp = "increasing",
            trendDown = "decreasing",
            trendStable = "stable",
            minValue = "Min",
            maxValue = "Max",
            avgValue = "Avg",
            avgIntensity = "Avg intensity",
            maxIntensity = "Max intensity",
            commonTriggers = "Common triggers",
            abnormalReadings = "Abnormal readings",
            period = "Period",
            diastolicLabel = "Diastolic",
            noSymptoms = "No symptoms recorded for this period.",
            noVitals = "No vital signs recorded for this period.",
            noMedications = "No active medications.",
            noPatterns = "Not enough data for pattern analysis.",
            highBP = "elevated (hypertension)",
            lowBP = "low (hypotension)",
            highDiastolic = "diastolic elevated",
            lowDiastolic = "diastolic low",
            highPulse = "elevated (tachycardia)",
            lowPulse = "low (bradycardia)",
            fever = "fever",
            subfebrile = "slightly elevated",
            hypothermia = "low temperature",
            criticalO2 = "critically low oxygen",
            lowO2 = "low oxygen saturation",
            highGlucose = "significantly elevated glucose",
            elevatedGlucose = "elevated glucose",
            lowGlucose = "low glucose (hypoglycemia)",
            severeSymptoms = "Severe symptoms (7+/10)",
            recurringSymptom = "Recurring symptom",
            sameDayCorrelation = "Same-day correlation",
            diabetesGlucoseWarning = "Note: Elevated glucose readings with known diabetes \u2014 recommend discussing with your doctor.",
            hypertensionBPWarning = "Note: Elevated blood pressure with known hypertension \u2014 recommend discussing with your doctor.",
            disclaimer = "This analysis is based on your recorded health data using rule-based pattern detection."
        )

        val RU = AnalysisTexts(
            summaryTitle = "[\u0421\u0432\u043E\u0434\u043A\u0430 \u0434\u0430\u043D\u043D\u044B\u0445 \u043E \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u0435]",
            patternTitle = "[\u0410\u043D\u0430\u043B\u0438\u0437 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u043E\u0432]",
            localAnalysisNote = "\u0410\u043D\u0430\u043B\u0438\u0437 \u0432\u044B\u043F\u043E\u043B\u043D\u0435\u043D \u043B\u043E\u043A\u0430\u043B\u044C\u043D\u043E \u043D\u0430 \u0432\u0430\u0448\u0435\u043C \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0435. \u0412\u0430\u0448\u0438 \u0434\u0430\u043D\u043D\u044B\u0435 \u043E\u0441\u0442\u0430\u044E\u0442\u0441\u044F \u043A\u043E\u043D\u0444\u0438\u0434\u0435\u043D\u0446\u0438\u0430\u043B\u044C\u043D\u044B\u043C\u0438.",
            patientInfo = "\u0418\u041D\u0424\u041E \u041E \u041F\u0410\u0426\u0418\u0415\u041D\u0422\u0415",
            symptomsSection = "\u0421\u0418\u041C\u041F\u0422\u041E\u041C\u042B",
            vitalsSection = "\u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0418",
            medicationsSection = "\u041B\u0415\u041A\u0410\u0420\u0421\u0422\u0412\u0410",
            observationsSection = "\u041D\u0410\u0411\u041B\u042E\u0414\u0415\u041D\u0418\u042F",
            correlationsSection = "\u041A\u041E\u0420\u0420\u0415\u041B\u042F\u0426\u0418\u0418",
            vitalTrends = "\u0422\u0420\u0415\u041D\u0414\u042B \u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0415\u0419",
            symptomFrequency = "\u0427\u0410\u0421\u0422\u041E\u0422\u0410 \u0421\u0418\u041C\u041F\u0422\u041E\u041C\u041E\u0412",
            ageLabel = "Возраст",
            genderLabel = "Пол",
            weightLabel = "\u0412\u0435\u0441",
            heightLabel = "\u0420\u043E\u0441\u0442",
            knownConditions = "\u0418\u0437\u0432\u0435\u0441\u0442\u043D\u044B\u0435 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F",
            analyzingDisease = "Анализируемое заболевание",
            intensity = "\u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            triggers = "\u0422\u0440\u0438\u0433\u0433\u0435\u0440\u044B",
            notes = "\u0417\u0430\u043C\u0435\u0442\u043A\u0438",
            trend = "\u0422\u0440\u0435\u043D\u0434",
            trendUp = "\u0440\u043E\u0441\u0442",
            trendDown = "\u0441\u043D\u0438\u0436\u0435\u043D\u0438\u0435",
            trendStable = "\u0441\u0442\u0430\u0431\u0438\u043B\u044C\u043D\u043E",
            minValue = "\u041C\u0438\u043D",
            maxValue = "\u041C\u0430\u043A\u0441",
            avgValue = "\u0421\u0440\u0435\u0434\u043D",
            avgIntensity = "\u0421\u0440. \u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            maxIntensity = "\u041C\u0430\u043A\u0441. \u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            commonTriggers = "\u0427\u0430\u0441\u0442\u044B\u0435 \u0442\u0440\u0438\u0433\u0433\u0435\u0440\u044B",
            abnormalReadings = "\u041E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u0438\u044F \u043E\u0442 \u043D\u043E\u0440\u043C\u044B",
            period = "\u041F\u0435\u0440\u0438\u043E\u0434",
            diastolicLabel = "\u0414\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435",
            noSymptoms = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u0430\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.",
            noVitals = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u044F\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.",
            noMedications = "\u041D\u0435\u0442 \u0430\u043A\u0442\u0438\u0432\u043D\u044B\u0445 \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432.",
            noPatterns = "\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0434\u0430\u043D\u043D\u044B\u0445 \u0434\u043B\u044F \u0430\u043D\u0430\u043B\u0438\u0437\u0430 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u043E\u0432.",
            highBP = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u043E\u0435 (\u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D\u0438\u044F)",
            lowBP = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u043E\u0435 (\u0433\u0438\u043F\u043E\u0442\u043E\u043D\u0438\u044F)",
            highDiastolic = "\u0434\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435 \u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043E",
            lowDiastolic = "\u0434\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435 \u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043E",
            highPulse = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u044B\u0439 (\u0442\u0430\u0445\u0438\u043A\u0430\u0440\u0434\u0438\u044F)",
            lowPulse = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u044B\u0439 (\u0431\u0440\u0430\u0434\u0438\u043A\u0430\u0440\u0434\u0438\u044F)",
            fever = "\u043B\u0438\u0445\u043E\u0440\u0430\u0434\u043A\u0430",
            subfebrile = "\u0441\u0443\u0431\u0444\u0435\u0431\u0440\u0438\u043B\u044C\u043D\u0430\u044F",
            hypothermia = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0442\u0435\u043C\u043F\u0435\u0440\u0430\u0442\u0443\u0440\u0430",
            criticalO2 = "\u043A\u0440\u0438\u0442\u0438\u0447\u0435\u0441\u043A\u0438 \u043D\u0438\u0437\u043A\u0438\u0439 \u043A\u0438\u0441\u043B\u043E\u0440\u043E\u0434",
            lowO2 = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0441\u0430\u0442\u0443\u0440\u0430\u0446\u0438\u044F",
            highGlucose = "\u0437\u043D\u0430\u0447\u0438\u0442\u0435\u043B\u044C\u043D\u043E \u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430",
            elevatedGlucose = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430",
            lowGlucose = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430 (\u0433\u0438\u043F\u043E\u0433\u043B\u0438\u043A\u0435\u043C\u0438\u044F)",
            severeSymptoms = "\u0422\u044F\u0436\u0451\u043B\u044B\u0435 \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u044B (7+/10)",
            recurringSymptom = "\u041F\u043E\u0432\u0442\u043E\u0440\u044F\u044E\u0449\u0438\u0439\u0441\u044F \u0441\u0438\u043C\u043F\u0442\u043E\u043C",
            sameDayCorrelation = "\u0421\u043E\u0432\u043F\u0430\u0434\u0435\u043D\u0438\u0435 \u0432 \u043E\u0434\u0438\u043D \u0434\u0435\u043D\u044C",
            diabetesGlucoseWarning = "\u041F\u0440\u0438\u043C\u0435\u0447\u0430\u043D\u0438\u0435: \u041F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430 \u043F\u0440\u0438 \u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u043C \u0434\u0438\u0430\u0431\u0435\u0442\u0435 \u2014 \u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C.",
            hypertensionBPWarning = "\u041F\u0440\u0438\u043C\u0435\u0447\u0430\u043D\u0438\u0435: \u041F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u043E\u0435 \u0434\u0430\u0432\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438 \u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u0439 \u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D\u0438\u0438 \u2014 \u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C.",
            disclaimer = "\u0410\u043D\u0430\u043B\u0438\u0437 \u043E\u0441\u043D\u043E\u0432\u0430\u043D \u043D\u0430 \u0432\u0430\u0448\u0438\u0445 \u0437\u0430\u043F\u0438\u0441\u0430\u043D\u043D\u044B\u0445 \u0434\u0430\u043D\u043D\u044B\u0445 \u043E \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u0435 \u0441 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u0435\u043C \u0430\u043D\u0430\u043B\u0438\u0437\u0430 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u043E\u0432."
        )

        val ES = AnalysisTexts(
            summaryTitle = "[Resumen de datos de salud]",
            patternTitle = "[An\u00E1lisis de patrones]",
            localAnalysisNote = "Analizado localmente en su dispositivo. Sus datos permanecen privados.",
            patientInfo = "INFO DEL PACIENTE",
            symptomsSection = "S\u00CDNTOMAS",
            vitalsSection = "SIGNOS VITALES",
            medicationsSection = "MEDICAMENTOS",
            observationsSection = "OBSERVACIONES",
            correlationsSection = "CORRELACIONES",
            vitalTrends = "TENDENCIAS DE SIGNOS VITALES",
            symptomFrequency = "FRECUENCIA DE S\u00CDNTOMAS",
            ageLabel = "Edad",
            genderLabel = "Género",
            weightLabel = "Peso",
            heightLabel = "Altura",
            knownConditions = "Condiciones conocidas",
            analyzingDisease = "Enfermedad analizada",
            intensity = "intensidad",
            triggers = "Desencadenantes",
            notes = "Notas",
            trend = "Tendencia",
            trendUp = "en aumento",
            trendDown = "en descenso",
            trendStable = "estable",
            minValue = "M\u00EDn",
            maxValue = "M\u00E1x",
            avgValue = "Prom",
            avgIntensity = "Intensidad prom",
            maxIntensity = "Intensidad m\u00E1x",
            commonTriggers = "Desencadenantes comunes",
            abnormalReadings = "Lecturas anormales",
            period = "Per\u00EDodo",
            diastolicLabel = "Diast\u00F3lica",
            noSymptoms = "No hay registros de s\u00EDntomas para este per\u00EDodo.",
            noVitals = "No hay registros de signos vitales para este per\u00EDodo.",
            noMedications = "No hay medicamentos activos.",
            noPatterns = "No hay suficientes datos para el an\u00E1lisis de patrones.",
            highBP = "elevada (hipertensi\u00F3n)",
            lowBP = "baja (hipotensi\u00F3n)",
            highDiastolic = "diast\u00F3lica elevada",
            lowDiastolic = "diast\u00F3lica baja",
            highPulse = "elevado (taquicardia)",
            lowPulse = "bajo (bradicardia)",
            fever = "fiebre",
            subfebrile = "ligeramente elevada",
            hypothermia = "temperatura baja",
            criticalO2 = "ox\u00EDgeno cr\u00EDticamente bajo",
            lowO2 = "saturaci\u00F3n de ox\u00EDgeno baja",
            highGlucose = "glucosa significativamente elevada",
            elevatedGlucose = "glucosa elevada",
            lowGlucose = "glucosa baja (hipoglucemia)",
            severeSymptoms = "S\u00EDntomas severos (7+/10)",
            recurringSymptom = "S\u00EDntoma recurrente",
            sameDayCorrelation = "Correlaci\u00F3n del mismo d\u00EDa",
            diabetesGlucoseWarning = "Nota: Lecturas de glucosa elevadas con diabetes conocida \u2014 se recomienda consultar con su m\u00E9dico.",
            hypertensionBPWarning = "Nota: Presi\u00F3n arterial elevada con hipertensi\u00F3n conocida \u2014 se recomienda consultar con su m\u00E9dico.",
            disclaimer = "Este an\u00E1lisis se basa en sus datos de salud registrados mediante detecci\u00F3n de patrones."
        )

        val ZH_CN = AnalysisTexts(
            summaryTitle = "[\u5065\u5EB7\u6570\u636E\u6458\u8981]",
            patternTitle = "[\u6A21\u5F0F\u5206\u6790]",
            localAnalysisNote = "\u5728\u60A8\u7684\u8BBE\u5907\u4E0A\u672C\u5730\u5206\u6790\u3002\u60A8\u7684\u6570\u636E\u4FDD\u6301\u79C1\u5BC6\u3002",
            patientInfo = "\u60A3\u8005\u4FE1\u606F",
            symptomsSection = "\u75C7\u72B6",
            vitalsSection = "\u751F\u547D\u4F53\u5F81",
            medicationsSection = "\u836F\u7269",
            observationsSection = "\u89C2\u5BDF\u7ED3\u679C",
            correlationsSection = "\u76F8\u5173\u6027",
            vitalTrends = "\u751F\u547D\u4F53\u5F81\u8D8B\u52BF",
            symptomFrequency = "\u75C7\u72B6\u9891\u7387",
            ageLabel = "年龄",
            genderLabel = "性别",
            weightLabel = "\u4F53\u91CD",
            heightLabel = "\u8EAB\u9AD8",
            knownConditions = "\u5DF2\u77E5\u75BE\u75C5",
            analyzingDisease = "分析中的疾病",
            intensity = "\u5F3A\u5EA6",
            triggers = "\u8BF1\u56E0",
            notes = "\u5907\u6CE8",
            trend = "\u8D8B\u52BF",
            trendUp = "\u4E0A\u5347",
            trendDown = "\u4E0B\u964D",
            trendStable = "\u7A33\u5B9A",
            minValue = "\u6700\u5C0F",
            maxValue = "\u6700\u5927",
            avgValue = "\u5E73\u5747",
            avgIntensity = "\u5E73\u5747\u5F3A\u5EA6",
            maxIntensity = "\u6700\u5927\u5F3A\u5EA6",
            commonTriggers = "\u5E38\u89C1\u8BF1\u56E0",
            abnormalReadings = "\u5F02\u5E38\u8BFB\u6570",
            period = "\u65F6\u95F4\u6BB5",
            diastolicLabel = "\u8212\u5F20\u538B",
            noSymptoms = "\u8BE5\u671F\u95F4\u65E0\u75C7\u72B6\u8BB0\u5F55\u3002",
            noVitals = "\u8BE5\u671F\u95F4\u65E0\u751F\u547D\u4F53\u5F81\u8BB0\u5F55\u3002",
            noMedications = "\u65E0\u6B63\u5728\u4F7F\u7528\u7684\u836F\u7269\u3002",
            noPatterns = "\u6570\u636E\u4E0D\u8DB3\uFF0C\u65E0\u6CD5\u8FDB\u884C\u6A21\u5F0F\u5206\u6790\u3002",
            highBP = "\u5347\u9AD8\uFF08\u9AD8\u8840\u538B\uFF09",
            lowBP = "\u504F\u4F4E\uFF08\u4F4E\u8840\u538B\uFF09",
            highDiastolic = "\u8212\u5F20\u538B\u5347\u9AD8",
            lowDiastolic = "\u8212\u5F20\u538B\u504F\u4F4E",
            highPulse = "\u5347\u9AD8\uFF08\u5FC3\u52A8\u8FC7\u901F\uFF09",
            lowPulse = "\u504F\u4F4E\uFF08\u5FC3\u52A8\u8FC7\u7F13\uFF09",
            fever = "\u53D1\u70ED",
            subfebrile = "\u8F7B\u5FAE\u5347\u9AD8",
            hypothermia = "\u4F53\u6E29\u504F\u4F4E",
            criticalO2 = "\u6C27\u6C14\u4E25\u91CD\u4E0D\u8DB3",
            lowO2 = "\u8840\u6C27\u9971\u548C\u5EA6\u504F\u4F4E",
            highGlucose = "\u8840\u7CD6\u663E\u8457\u5347\u9AD8",
            elevatedGlucose = "\u8840\u7CD6\u5347\u9AD8",
            lowGlucose = "\u8840\u7CD6\u504F\u4F4E\uFF08\u4F4E\u8840\u7CD6\uFF09",
            severeSymptoms = "\u4E25\u91CD\u75C7\u72B6 (7+/10)",
            recurringSymptom = "\u53CD\u590D\u51FA\u73B0\u7684\u75C7\u72B6",
            sameDayCorrelation = "\u540C\u65E5\u76F8\u5173\u6027",
            diabetesGlucoseWarning = "\u6CE8\u610F\uFF1A\u5DF2\u77E5\u7CD6\u5C3F\u75C5\u60A3\u8005\u8840\u7CD6\u5347\u9AD8 \u2014 \u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u3002",
            hypertensionBPWarning = "\u6CE8\u610F\uFF1A\u5DF2\u77E5\u9AD8\u8840\u538B\u60A3\u8005\u8840\u538B\u5347\u9AD8 \u2014 \u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u3002",
            disclaimer = "\u6B64\u5206\u6790\u57FA\u4E8E\u60A8\u8BB0\u5F55\u7684\u5065\u5EB7\u6570\u636E\uFF0C\u4F7F\u7528\u89C4\u5219\u6A21\u5F0F\u68C0\u6D4B\u3002"
        )

        val HY = AnalysisTexts(
            summaryTitle = "[\u0531\u057C\u0578\u0572\u056A\u0561\u056F\u0561\u0576 \u057F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u056B \u0561\u0574\u0583\u0578\u0583\u0578\u0582\u0574]",
            patternTitle = "[\u0555\u0580\u056B\u0576\u0561\u0579\u0561\u0583\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0576\u0565\u0580\u056B \u057E\u0565\u0580\u056C\u0578\u0582\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576]",
            localAnalysisNote = "\u054E\u0565\u0580\u056C\u0578\u0582\u056E\u057E\u0561\u056E \u0567 \u057F\u0565\u0572\u0561\u0575\u0576\u043E\u0580\u0565\u0576\u055D \u0571\u0565\u0580 \u057D\u0561\u0580\u0584\u0578\u0582\u043C\u0589 \u0541\u0565\u0580 \u057F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u0568 \u043C\u0576\u0578\u0582\u043C \u0565\u0576 \u0563\u0561\u0572\u057F\u0576\u056B\u0589",
            patientInfo = "\u054F\u0535\u0542\u0535\u053F\u0531\u054F\u054E\u0548\u0552\u0539\u0546 \u0540\u053B\u054E\u0531\u0546\u0534\u053B \u0544\u0531\u054D\u053B\u0546",
            symptomsSection = "\u0531\u053D\u054F\u0531\u0546\u053B\u0547\u0546\u0535\u054C",
            vitalsSection = "\u053F\u0535\u0546\u054D\u0531\u053F\u0531\u0546 \u0551\u0548\u0552\u053B\u0549\u0546\u0535\u054C",
            medicationsSection = "\u0534\u0535\u0542\u0535\u054C",
            observationsSection = "\u0534\u053B\u054F\u0531\u054C\u053F\u0548\u0552\u0544\u0546\u0535\u054C",
            correlationsSection = "\u0540\u0531\u054C\u0531\u0532\u0535\u054C\u0531\u053F\u0551\u0548\u0552\u054F\u0545\u0548\u0552\u0546\u0546\u0535\u054C",
            vitalTrends = "\u053F\u0535\u0546\u054D\u0531\u053F\u0531\u0546 \u0551\u0548\u0552\u053B\u0549\u0546\u0535\u054C\u053B \u0544\u053B\u054F\u0548\u0552\u0544\u0546\u0535\u054C",
            symptomFrequency = "\u0531\u053D\u054F\u0531\u0546\u053B\u0547\u0546\u0535\u054C\u053B \u0540\u0531\u0552\u053D\u0531\u053F\u0531\u0546\u0548\u0552\u054F\u0545\u0548\u0552\u0546",
            ageLabel = "\u054F\u0561\u0580\u056B\u0584",
            genderLabel = "\u054D\u0565\u057C",
            weightLabel = "\u0554\u0561\u0577",
            heightLabel = "\u0540\u0561\u057D\u0561\u056F",
            knownConditions = "\u0540\u0561\u0575\u057F\u0576\u056B \u0570\u056B\u057E\u0561\u0576\u0564\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0576\u0565\u0580",
            analyzingDisease = "\u054E\u0565\u0580\u056C\u0578\u0582\u056E\u057E\u0578\u0572 \u0570\u056B\u057E\u0561\u0576\u0564\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            intensity = "\u056B\u0576\u057F\u0565\u0576\u057D\u056B\u057E\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            triggers = "\u054A\u0561\u057F\u0573\u0561\u057C\u0576\u0565\u0580",
            notes = "\u0546\u0578\u057F\u0561\u0576\u0565\u0580",
            trend = "\u0544\u056B\u057F\u0578\u0582\u043C",
            trendUp = "\u0561\u0573",
            trendDown = "\u0576\u057E\u0561\u0566\u0578\u0582\u043C",
            trendStable = "\u056F\u0561\u0575\u0578\u0582\u0576",
            minValue = "\u0546\u057E\u0561\u0566",
            maxValue = "\u0531\u057C\u0561\u057E",
            avgValue = "\u0544\u056B\u056A\u056B\u0576",
            avgIntensity = "\u0544\u056B\u056A. \u056B\u0576\u057F\u0565\u0576\u057D\u056B\u057E\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            maxIntensity = "\u0531\u057C\u0561\u057E. \u056B\u0576\u057F\u0565\u0576\u057D\u056B\u057E\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            commonTriggers = "\u0540\u0561\u0573\u0561\u056D \u043F\u0561\u057F\u0573\u0561\u057C\u0576\u0565\u0580",
            abnormalReadings = "\u0547\u0565\u0572\u0578\u0582\u043C\u0576\u0565\u0580 \u043D\u043E\u0580\u043C\u056B\u0581",
            period = "\u054A\u0561\u0580\u0562\u0565\u0580\u0561\u0577\u0580\u056A\u0561\u0576",
            diastolicLabel = "\u054D\u057F\u043E\u0580\u056B\u0576 \u0573\u0576\u0577\u0578\u0582\u043C",
            noSymptoms = "\u0531\u0575\u057D \u056A\u0561\u043C\u0561\u0576\u0561\u056F\u0561\u0570\u0561\u057F\u057E\u0561\u056E\u0578\u0582\u043C \u0561\u056D\u057F\u0561\u0576\u056B\u0577\u0576\u0565\u0580\u056B \u0563\u0580\u0561\u0576\u0581\u0578\u0582\u043C\u0576\u0565\u0580 \u0579\u056F\u0561\u0576\u0589",
            noVitals = "\u0531\u0575\u057D \u056A\u0561\u043C\u0561\u0576\u0561\u056F\u0561\u0570\u0561\u057F\u057E\u0561\u056E\u0578\u0582\u043C \u056F\u0565\u0576\u057D\u0561\u056F\u0561\u0576 \u0581\u0578\u0582\u0581\u056B\u0579\u0576\u0565\u0580\u056B \u0563\u0580\u0561\u0576\u0581\u0578\u0582\u043C\u0576\u0565\u0580 \u0579\u056F\u0561\u0576\u0589",
            noMedications = "\u0531\u056F\u057F\u056B\u057E \u0564\u0565\u0572\u0565\u0580 \u0579\u056F\u0561\u0576\u0589",
            noPatterns = "\u054F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u0568 \u0562\u0561\u057E\u0561\u0580\u0561\u0580 \u0579\u0565\u0576 \u0585\u0580\u056B\u0576\u0561\u0579\u0561\u0583\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0576\u0565\u0580\u056B \u057E\u0565\u0580\u056C\u0578\u0582\u056E\u0578\u0582\u0569\u0575\u0561\u0576 \u0570\u0561\u043C\u0561\u0580\u0589",
            highBP = "\u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E (\u0570\u056B\u043F\u0565\u0580\u057F\u043E\u0576\u056B\u0561)",
            lowBP = "\u0581\u0561\u056E\u0580 (\u0570\u056B\u043F\u043E\u057F\u043E\u0576\u056B\u0561)",
            highDiastolic = "\u057D\u057F\u043E\u0580\u056B\u0576 \u0573\u0576\u0577\u0578\u0582\u043C\u0568 \u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E",
            lowDiastolic = "\u057D\u057F\u043E\u0580\u056B\u0576 \u0573\u0576\u0577\u0578\u0582\u043C\u0568 \u0581\u0561\u056E\u0580",
            highPulse = "\u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E (\u057F\u0561\u056D\u056B\u056F\u0561\u0580\u0564\u056B\u0561)",
            lowPulse = "\u0581\u0561\u056E\u0580 (\u0562\u0580\u0561\u0564\u056B\u056F\u0561\u0580\u0564\u056B\u0561)",
            fever = "\u056B\u0565\u0580\u043C\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            subfebrile = "\u0569\u0565\u0569\u0587\u0561\u056F\u056B \u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E",
            hypothermia = "\u0581\u0561\u056E\u0580 \u056A\u0565\u0580\u043C\u0561\u057D\u057F\u056B\u0573\u0561\u0576",
            criticalO2 = "\u056F\u0580\u056B\u057F\u056B\u056F\u0561\u056F\u0561\u0576 \u0581\u0561\u056E\u0580 \u0569\u0569\u057E\u0561\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            lowO2 = "\u0581\u0561\u056E\u0580 \u0569\u0569\u057E\u0561\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            highGlucose = "\u0566\u0563\u0561\u056C\u056B\u043E\u0580\u0565\u0576 \u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E \u0563\u056C\u0575\u0578\u0582\u056F\u043E\u0566",
            elevatedGlucose = "\u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0561\u056E \u0563\u056C\u0575\u0578\u0582\u056F\u043E\u0566",
            lowGlucose = "\u0581\u0561\u056E\u0580 \u0563\u056C\u0575\u0578\u0582\u056F\u043E\u0566 (\u0570\u056B\u043F\u043E\u0563\u056C\u056B\u056F\u0565\u043C\u056B\u0561)",
            severeSymptoms = "\u053E\u0561\u0576\u0580 \u0561\u056D\u057F\u0561\u0576\u056B\u0577\u0576\u0565\u0580 (7+/10)",
            recurringSymptom = "\u053F\u0580\u056F\u0576\u057E\u0578\u0572 \u0561\u056D\u057F\u0561\u0576\u056B\u0577",
            sameDayCorrelation = "\u0546\u043E\u0575\u0576 \u0585\u0580\u057E\u0561 \u0570\u0561\u0580\u0561\u0562\u0565\u0580\u0561\u056F\u0581\u0578\u0582\u0569\u0575\u0578\u0582\u0576",
            diabetesGlucoseWarning = "\u0548\u0582\u0577\u0561\u0564\u0580\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u055D \u0570\u0561\u0575\u057F\u0576\u056B \u0577\u0561\u0584\u0561\u0580\u0561\u056D\u057F\u0578\u057E \u0570\u056B\u057E\u0561\u0576\u0564\u056B \u0564\u0565\u043F\u0584\u0578\u0582\u043C \u0563\u056C\u0575\u0578\u0582\u056F\u043E\u0566\u056B \u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0578\u0582\u043C \u2014 \u056D\u043E\u0580\u0570\u0578\u0582\u0580\u0564\u0561\u056F\u0581\u057E\u0578\u0582\u043C \u0567 \u0562\u056A\u0577\u056F\u056B \u0570\u0565\u057F \u0584\u0576\u0576\u0561\u0580\u056F\u0565\u056C\u0589",
            hypertensionBPWarning = "\u0548\u0582\u0577\u0561\u0564\u0580\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u055D \u0570\u0561\u0575\u057F\u0576\u056B \u0570\u056B\u043F\u0565\u0580\u057F\u043E\u0576\u056B\u0561\u0575\u056B \u0564\u0565\u043F\u0584\u0578\u0582\u043C \u0561\u0580\u0575\u0561\u0576 \u0573\u0576\u0577\u043C\u0561\u0576 \u0562\u0561\u0580\u0571\u0580\u0561\u0581\u0578\u0582\u043C \u2014 \u056D\u043E\u0580\u0570\u0578\u0582\u0580\u0564\u0561\u056F\u0581\u057E\u0578\u0582\u043C \u0567 \u0562\u056A\u0577\u056F\u056B \u0570\u0565\u057F \u0584\u0576\u0576\u0561\u0580\u056F\u0565\u056C\u0589",
            disclaimer = "\u054E\u0565\u0580\u056C\u0578\u0582\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0568 \u0570\u056B\u043C\u0576\u057E\u0561\u056E \u0567 \u0571\u0565\u0580 \u0563\u0580\u0561\u0576\u0581\u057E\u0561\u056E \u0561\u057C\u043E\u0572\u056A\u0561\u043F\u0561\u0570\u0561\u056F\u0561\u0576 \u057F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u056B \u0570\u056B\u043C\u0561\u0576 \u057E\u0580\u0561\u055D \u043E\u0580\u056B\u0576\u0561\u0579\u0561\u0583\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0576\u0565\u0580\u056B \u0570\u0561\u0575\u057F\u0576\u0561\u0562\u0565\u0580\u043C\u0561\u043C\u0562\u0589"
        )

        fun forLanguage(lang: String): AnalysisTexts = when (lang) {
            "en" -> EN
            "es" -> ES
            "zh-CN" -> ZH_CN
            "hy" -> HY
            else -> RU
        }
    }
}
