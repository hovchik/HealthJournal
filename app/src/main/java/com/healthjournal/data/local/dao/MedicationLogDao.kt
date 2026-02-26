package com.healthjournal.data.local.dao

import androidx.room.*
import com.healthjournal.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC")
    fun getByMedicationId(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Insert
    suspend fun insert(entity: MedicationLogEntity): Long

    @Delete
    suspend fun delete(entity: MedicationLogEntity)
}
