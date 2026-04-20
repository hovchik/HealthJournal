package com.hovchik.healthjournal.domain.usecase

import com.hovchik.healthjournal.domain.model.Medication
import com.hovchik.healthjournal.domain.model.MedicationLog
import com.hovchik.healthjournal.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow

class GetActiveMedicationsUseCase constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> = repository.getActiveMedications()
}

class GetAllMedicationsUseCase constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<List<Medication>> = repository.getAllMedications()
}

class AddMedicationUseCase constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication): Long =
        repository.insertMedication(medication)
}

class UpdateMedicationUseCase constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) = repository.updateMedication(medication)
}

class GetMedicationByIdUseCase constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(id: Long): Medication? = repository.getMedicationById(id)
}

class DeleteMedicationUseCase constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication) = repository.deleteMedication(medication)
}

class LogMedicationTakenUseCase constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(log: MedicationLog): Long = repository.insertMedicationLog(log)
}

class GetMedicationLogsUseCase constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(medicationId: Long): Flow<List<MedicationLog>> =
        repository.getLogsForMedication(medicationId)
}
