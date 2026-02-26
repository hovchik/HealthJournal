package com.healthjournal.domain.model

import java.time.LocalDateTime

enum class VitalType(val displayName: String, val unit: String) {
    BLOOD_PRESSURE("Давление", "мм рт.ст."),
    PULSE("Пульс", "уд/мин"),
    TEMPERATURE("Температура", "°C"),
    SPO2("SpO2", "%"),
    GLUCOSE("Глюкоза", "ммоль/л"),
    WEIGHT("Вес", "кг"),
    SLEEP("Сон", "ч"),
    STEPS("Шаги", "шагов")
}

data class VitalSign(
    val id: Long = 0,
    val type: VitalType,
    val value: Double,
    val secondaryValue: Double? = null, // For blood pressure (diastolic)
    val notes: String = "",
    val recordedAt: LocalDateTime = LocalDateTime.now()
)
