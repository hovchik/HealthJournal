package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.MedicationDto
import com.healthjournal.data.local.dto.MedicationLogDto
import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.MedicationLog
import com.healthjournal.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicationRepositoryImpl constructor(
    private val medicationStore: JsonFileStore<MedicationDto>,
    private val logStore: JsonFileStore<MedicationLogDto>
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> =
        medicationStore.observeAll().map { list ->
            list.sortedBy { it.name }.map { it.toDomain() }
        }

    override fun getActiveMedications(): Flow<List<Medication>> =
        medicationStore.observe { it.active }
            .map { list -> list.sortedBy { it.name }.map { it.toDomain() } }

    override suspend fun getMedicationById(id: Long): Medication? =
        medicationStore.getById(id)?.toDomain()

    override suspend fun insertMedication(medication: Medication): Long =
        medicationStore.insert(MedicationDto.from(medication))

    override suspend fun updateMedication(medication: Medication) =
        medicationStore.update(MedicationDto.from(medication))

    override suspend fun deleteMedication(medication: Medication) {
        medicationStore.delete(MedicationDto.from(medication))
        logStore.deleteWhere { it.medicationId == medication.id }
    }

    override fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>> =
        logStore.observe { it.medicationId == medicationId }
            .map { list -> list.sortedByDescending { it.takenAt }.map { it.toDomain() } }

    override suspend fun insertMedicationLog(log: MedicationLog): Long =
        logStore.insert(MedicationLogDto.from(log))

    override suspend fun deleteMedicationLog(log: MedicationLog) =
        logStore.delete(MedicationLogDto.from(log))
}
