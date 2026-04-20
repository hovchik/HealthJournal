package com.hovchik.healthjournal.domain.usecase

import com.hovchik.healthjournal.domain.model.Symptom
import com.hovchik.healthjournal.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class GetAllSymptomsUseCase constructor(
    private val repository: SymptomRepository
) {
    operator fun invoke(): Flow<List<Symptom>> = repository.getAllSymptoms()
}

class GetSymptomsByDateRangeUseCase constructor(
    private val repository: SymptomRepository
) {
    operator fun invoke(from: LocalDateTime, to: LocalDateTime): Flow<List<Symptom>> =
        repository.getSymptomsByDateRange(from, to)
}

class AddSymptomUseCase constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(symptom: Symptom): Long = repository.insertSymptom(symptom)
}

class UpdateSymptomUseCase constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(symptom: Symptom) = repository.updateSymptom(symptom)
}

class GetSymptomByIdUseCase constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(id: Long): Symptom? = repository.getSymptomById(id)
}

class DeleteSymptomUseCase constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(symptom: Symptom) = repository.deleteSymptom(symptom)
}
