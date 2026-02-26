package com.healthjournal.data.repository

import com.healthjournal.data.local.dao.MedicationDao
import com.healthjournal.data.local.dao.MedicationLogDao
import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.MedicationLog
import com.healthjournal.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val medicationLogDao: MedicationLogDao
) : MedicationRepository {

    override fun getAllMedications(): Flow<List<Medication>> =
        medicationDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getActiveMedications(): Flow<List<Medication>> =
        medicationDao.getActive().map { list -> list.map { it.toDomain() } }

    override suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    override suspend fun insertMedication(medication: Medication): Long =
        medicationDao.insert(medication.toEntity())

    override suspend fun updateMedication(medication: Medication) =
        medicationDao.update(medication.toEntity())

    override suspend fun deleteMedication(medication: Medication) =
        medicationDao.delete(medication.toEntity())

    override fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>> =
        medicationLogDao.getByMedicationId(medicationId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertMedicationLog(log: MedicationLog): Long =
        medicationLogDao.insert(log.toEntity())

    override suspend fun deleteMedicationLog(log: MedicationLog) =
        medicationLogDao.delete(log.toEntity())
}
