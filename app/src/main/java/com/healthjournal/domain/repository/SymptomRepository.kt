package com.healthjournal.domain.repository

import com.healthjournal.domain.model.Symptom
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface SymptomRepository {
    fun getAllSymptoms(): Flow<List<Symptom>>
    fun getSymptomsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<Symptom>>
    suspend fun getSymptomById(id: Long): Symptom?
    suspend fun insertSymptom(symptom: Symptom): Long
    suspend fun updateSymptom(symptom: Symptom)
    suspend fun deleteSymptom(symptom: Symptom)
    suspend fun softDeleteByDiseaseId(diseaseId: Long)
    suspend fun restoreByDiseaseId(diseaseId: Long)
}
