package com.hovchik.healthjournal.data.repository

import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.VitalSignDto
import com.hovchik.healthjournal.domain.model.VitalSign
import com.hovchik.healthjournal.domain.model.VitalType
import com.hovchik.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId

class VitalSignRepositoryImpl constructor(
    private val store: JsonFileStore<VitalSignDto>
) : VitalSignRepository {

    override fun getAllVitalSigns(): Flow<List<VitalSign>> =
        store.observeAll().map { list ->
            list.filter { !it.isDeleted }.sortedByDescending { it.recordedAt }.map { it.toDomain() }
        }

    override fun getVitalSignsByType(type: VitalType): Flow<List<VitalSign>> =
        store.observe { it.type == type.name && !it.isDeleted }
            .map { list -> list.sortedByDescending { it.recordedAt }.map { it.toDomain() } }

    override fun getVitalSignsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<VitalSign>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMillis = to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return store.observe { it.recordedAt in fromMillis..toMillis && !it.isDeleted }
            .map { list -> list.sortedByDescending { it.recordedAt }.map { it.toDomain() } }
    }

    override suspend fun getVitalSignById(id: Long): VitalSign? =
        store.getById(id)?.toDomain()

    override suspend fun insertVitalSign(vitalSign: VitalSign): Long =
        store.insert(VitalSignDto.from(vitalSign))

    override suspend fun insertIfNotDuplicate(vitalSign: VitalSign): Long? {
        val dto = VitalSignDto.from(vitalSign)
        val existing = store.getAll()
        val isDuplicate = existing.any {
            it.type == dto.type &&
            it.recordedAt == dto.recordedAt &&
            it.value == dto.value &&
            it.profileId == dto.profileId &&
            !it.isDeleted
        }
        return if (isDuplicate) null else store.insert(dto)
    }

    override suspend fun updateVitalSign(vitalSign: VitalSign) =
        store.update(VitalSignDto.from(vitalSign))

    override suspend fun deleteVitalSign(vitalSign: VitalSign) =
        store.delete(VitalSignDto.from(vitalSign))

    override suspend fun softDeleteByDiseaseId(diseaseId: Long) =
        store.updateWhere({ it.diseaseId == diseaseId && !it.isDeleted }) { it.copy(isDeleted = true) }

    override suspend fun restoreByDiseaseId(diseaseId: Long) =
        store.updateWhere({ it.diseaseId == diseaseId && it.isDeleted }) { it.copy(isDeleted = false) }
}
