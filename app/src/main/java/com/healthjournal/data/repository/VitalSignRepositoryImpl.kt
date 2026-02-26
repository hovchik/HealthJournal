package com.healthjournal.data.repository

import com.healthjournal.data.local.dao.VitalSignDao
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class VitalSignRepositoryImpl @Inject constructor(
    private val dao: VitalSignDao
) : VitalSignRepository {

    override fun getAllVitalSigns(): Flow<List<VitalSign>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getVitalSignsByType(type: VitalType): Flow<List<VitalSign>> =
        dao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getVitalSignsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<VitalSign>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMillis = to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getByDateRange(fromMillis, toMillis).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getVitalSignById(id: Long): VitalSign? = dao.getById(id)?.toDomain()

    override suspend fun insertVitalSign(vitalSign: VitalSign): Long = dao.insert(vitalSign.toEntity())

    override suspend fun updateVitalSign(vitalSign: VitalSign) = dao.update(vitalSign.toEntity())

    override suspend fun deleteVitalSign(vitalSign: VitalSign) = dao.delete(vitalSign.toEntity())
}
