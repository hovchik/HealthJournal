package com.healthjournal.data.repository

import com.healthjournal.data.local.dao.SymptomDao
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SymptomRepositoryImpl @Inject constructor(
    private val dao: SymptomDao
) : SymptomRepository {

    override fun getAllSymptoms(): Flow<List<Symptom>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getSymptomsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<Symptom>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMillis = to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getByDateRange(fromMillis, toMillis).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSymptomById(id: Long): Symptom? = dao.getById(id)?.toDomain()

    override suspend fun insertSymptom(symptom: Symptom): Long = dao.insert(symptom.toEntity())

    override suspend fun updateSymptom(symptom: Symptom) = dao.update(symptom.toEntity())

    override suspend fun deleteSymptom(symptom: Symptom) = dao.delete(symptom.toEntity())
}
