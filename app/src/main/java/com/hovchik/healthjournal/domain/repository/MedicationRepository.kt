package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.Medication
import com.hovchik.healthjournal.domain.model.MedicationLog
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getAllMedications(): Flow<List<Medication>>
    fun getActiveMedications(): Flow<List<Medication>>
    suspend fun getMedicationById(id: Long): Medication?
    suspend fun insertMedication(medication: Medication): Long
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun softDeleteByDiseaseId(diseaseId: Long)
    suspend fun restoreByDiseaseId(diseaseId: Long)

    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>>
    suspend fun insertMedicationLog(log: MedicationLog): Long
    suspend fun deleteMedicationLog(log: MedicationLog)
}
