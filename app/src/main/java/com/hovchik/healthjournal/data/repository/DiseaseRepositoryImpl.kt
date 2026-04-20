package com.hovchik.healthjournal.data.repository

import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.DiseaseDto
import com.hovchik.healthjournal.domain.model.Disease
import com.hovchik.healthjournal.domain.repository.DiseaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class DiseaseRepositoryImpl(
    private val store: JsonFileStore<DiseaseDto>
) : DiseaseRepository {

    override fun getAllDiseases(): Flow<List<Disease>> =
        store.observeAll().map { list ->
            list.filter { !it.isDeleted }
                .sortedByDescending { it.createdAt }
                .map { it.toDomain() }
        }

    override fun getDiseasesByProfileId(profileId: Long): Flow<List<Disease>> =
        store.observe { it.profileId == profileId && !it.isDeleted }
            .map { list -> list.sortedByDescending { it.createdAt }.map { it.toDomain() } }

    override fun getDeletedDiseases(): Flow<List<Disease>> =
        store.observeAll().map { list ->
            list.filter { it.isDeleted }
                .sortedByDescending { it.deletedAt ?: it.createdAt }
                .map { it.toDomain() }
        }

    override fun getDeletedDiseasesByProfileId(profileId: Long): Flow<List<Disease>> =
        store.observe { it.profileId == profileId && it.isDeleted }
            .map { list -> list.sortedByDescending { it.deletedAt ?: it.createdAt }.map { it.toDomain() } }

    override suspend fun getDiseaseById(id: Long): Disease? =
        store.getById(id)?.toDomain()

    override suspend fun insertDisease(disease: Disease): Long =
        store.insert(DiseaseDto.from(disease))

    override suspend fun updateDisease(disease: Disease) =
        store.update(DiseaseDto.from(disease))

    override suspend fun deleteDisease(disease: Disease) =
        store.delete(DiseaseDto.from(disease))

    override suspend fun softDeleteDisease(disease: Disease) {
        val deleted = disease.copy(isDeleted = true, deletedAt = LocalDateTime.now())
        store.update(DiseaseDto.from(deleted))
    }

    override suspend fun restoreDisease(disease: Disease) {
        val restored = disease.copy(isDeleted = false, deletedAt = null)
        store.update(DiseaseDto.from(restored))
    }

    override suspend fun permanentlyDeleteDisease(disease: Disease) =
        store.delete(DiseaseDto.from(disease))
}
