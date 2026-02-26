package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.VitalSignDto
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class VitalSignRepositoryImpl @Inject constructor(
    private val store: JsonFileStore<VitalSignDto>
) : VitalSignRepository {

    override fun getAllVitalSigns(): Flow<List<VitalSign>> =
        store.observeAll().map { list ->
            list.sortedByDescending { it.recordedAt }.map { it.toDomain() }
        }

    override fun getVitalSignsByType(type: VitalType): Flow<List<VitalSign>> =
        store.observe { it.type == type.name }
            .map { list -> list.sortedByDescending { it.recordedAt }.map { it.toDomain() } }

    override fun getVitalSignsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<VitalSign>> {
        val fromMillis = from.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val toMillis = to.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return store.observe { it.recordedAt in fromMillis..toMillis }
            .map { list -> list.sortedByDescending { it.recordedAt }.map { it.toDomain() } }
    }

    override suspend fun getVitalSignById(id: Long): VitalSign? =
        store.getById(id)?.toDomain()

    override suspend fun insertVitalSign(vitalSign: VitalSign): Long =
        store.insert(VitalSignDto.from(vitalSign))

    override suspend fun updateVitalSign(vitalSign: VitalSign) =
        store.update(VitalSignDto.from(vitalSign))

    override suspend fun deleteVitalSign(vitalSign: VitalSign) =
        store.delete(VitalSignDto.from(vitalSign))
}
