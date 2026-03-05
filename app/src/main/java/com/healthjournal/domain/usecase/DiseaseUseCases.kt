package com.healthjournal.domain.usecase

import com.healthjournal.domain.model.Disease
import com.healthjournal.domain.repository.DiseaseRepository
import kotlinx.coroutines.flow.Flow

class GetAllDiseasesUseCase(private val repository: DiseaseRepository) {
    operator fun invoke(): Flow<List<Disease>> = repository.getAllDiseases()
}

class GetDiseasesByProfileIdUseCase(private val repository: DiseaseRepository) {
    operator fun invoke(profileId: Long): Flow<List<Disease>> =
        repository.getDiseasesByProfileId(profileId)
}

class GetDiseaseByIdUseCase(private val repository: DiseaseRepository) {
    suspend operator fun invoke(id: Long): Disease? = repository.getDiseaseById(id)
}

class AddDiseaseUseCase(private val repository: DiseaseRepository) {
    suspend operator fun invoke(disease: Disease): Long = repository.insertDisease(disease)
}

class UpdateDiseaseUseCase(private val repository: DiseaseRepository) {
    suspend operator fun invoke(disease: Disease) = repository.updateDisease(disease)
}

class DeleteDiseaseUseCase(private val repository: DiseaseRepository) {
    suspend operator fun invoke(disease: Disease) = repository.deleteDisease(disease)
}
