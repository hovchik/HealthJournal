package com.healthjournal.domain.model

import java.time.LocalDate

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val active: Boolean = true,
    val notes: String = "",
    val profileId: Long = 0,
    val diseaseId: Long = 0
)
