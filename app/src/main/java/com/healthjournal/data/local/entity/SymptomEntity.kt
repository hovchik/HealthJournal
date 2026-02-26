package com.healthjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val intensity: Int,
    val durationMinutes: Int?,
    val triggers: String, // JSON list stored as string
    val notes: String,
    val recordedAt: Long // epoch millis
)
