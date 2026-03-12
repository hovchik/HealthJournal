package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.SymptomDto
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId

class SymptomRepositoryImpl constructor(
    private val store: JsonFileStore<SymptomDto>
) : SymptomRepository {

    override fun getAllSymptoms(): Flow<List<Symptom>> =
        store.observeAll().map { list ->
            list.filter { !it.isDeleted }.sortedByDescending { it.recordedAt }.map { it.toDomain() }
        }

    override fun getSymptomsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<Symptom>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMillis = to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return store.observe { it.recordedAt in fromMillis..toMillis && !it.isDeleted }
            .map { list -> list.sortedByDescending { it.recordedAt }.map { it.toDomain() } }
    }

    override suspend fun getSymptomById(id: Long): Symptom? =
        store.getById(id)?.toDomain()

    override suspend fun insertSymptom(symptom: Symptom): Long =
        store.insert(SymptomDto.from(symptom))

    override suspend fun updateSymptom(symptom: Symptom) =
        store.update(SymptomDto.from(symptom))

    override suspend fun deleteSymptom(symptom: Symptom) =
        store.delete(SymptomDto.from(symptom))

    override suspend fun softDeleteByDiseaseId(diseaseId: Long) =
        store.updateWhere({ it.diseaseId == diseaseId && !it.isDeleted }) { it.copy(isDeleted = true) }

    override suspend fun restoreByDiseaseId(diseaseId: Long) =
        store.updateWhere({ it.diseaseId == diseaseId && it.isDeleted }) { it.copy(isDeleted = false) }
}
