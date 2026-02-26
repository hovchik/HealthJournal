package com.healthjournal.domain.usecase

import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.MedicationLog
import com.healthjournal.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> = repository.getActiveMedications()
}

class GetAllMedicationsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> = repository.getAllMedications()
}

class AddMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication): Long =
        repository.insertMedication(medication)
}

class UpdateMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) = repository.updateMedication(medication)
}

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) = repository.deleteMedication(medication)
}

class LogMedicationTakenUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(log: MedicationLog): Long = repository.insertMedicationLog(log)
}

class GetMedicationLogsUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(medicationId: Long): Flow<List<MedicationLog>> =
        repository.getLogsForMedication(medicationId)
}
