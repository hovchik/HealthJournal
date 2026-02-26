package com.healthjournal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthjournal.data.local.converter.Converters
import com.healthjournal.data.local.dao.*
import com.healthjournal.data.local.entity.*

@Database(
    entities = [
        SymptomEntity::class,
        VitalSignEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        AiReportEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao
    abstract fun vitalSignDao(): VitalSignDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun aiReportDao(): AiReportDao
}
