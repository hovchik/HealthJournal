package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.DiseaseDto
import com.healthjournal.domain.model.Disease
import com.healthjournal.domain.repository.DiseaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiseaseRepositoryImpl(
    private val store: JsonFileStore<DiseaseDto>
) : DiseaseRepository {

    override fun getAllDiseases(): Flow<List<Disease>> =
        store.observeAll().map { list ->
            list.sortedByDescending { it.createdAt }.map { it.toDomain() }
        }

    override fun getDiseasesByProfileId(profileId: Long): Flow<List<Disease>> =
        store.observe { it.profileId == profileId }
            .map { list -> list.sortedByDescending { it.createdAt }.map { it.toDomain() } }

    override suspend fun getDiseaseById(id: Long): Disease? =
        store.getById(id)?.toDomain()

    override suspend fun insertDisease(disease: Disease): Long =
        store.insert(DiseaseDto.from(disease))

    override suspend fun updateDisease(disease: Disease) =
        store.update(DiseaseDto.from(disease))

    override suspend fun deleteDisease(disease: Disease) =
        store.delete(DiseaseDto.from(disease))
}
