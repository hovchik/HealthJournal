package com.healthjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vital_signs")
data class VitalSignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // VitalType enum name
    val value: Double,
    val secondaryValue: Double?,
    val notes: String,
    val recordedAt: Long // epoch millis
)
