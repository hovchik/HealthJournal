package com.healthjournal.domain.repository

import com.healthjournal.domain.model.Disease
import kotlinx.coroutines.flow.Flow

interface DiseaseRepository {
    fun getAllDiseases(): Flow<List<Disease>>
    fun getDiseasesByProfileId(profileId: Long): Flow<List<Disease>>
    fun getDeletedDiseases(): Flow<List<Disease>>
    fun getDeletedDiseasesByProfileId(profileId: Long): Flow<List<Disease>>
    suspend fun getDiseaseById(id: Long): Disease?
    suspend fun insertDisease(disease: Disease): Long
    suspend fun updateDisease(disease: Disease)
    suspend fun deleteDisease(disease: Disease)
    suspend fun softDeleteDisease(disease: Disease)
    suspend fun restoreDisease(disease: Disease)
    suspend fun permanentlyDeleteDisease(disease: Disease)
}
