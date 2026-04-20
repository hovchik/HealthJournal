package com.hovchik.healthjournal.domain.usecase

import com.hovchik.healthjournal.domain.model.VitalSign
import com.hovchik.healthjournal.domain.model.VitalType
import com.hovchik.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class GetAllVitalSignsUseCase constructor(
    private val repository: VitalSignRepository
) {
    operator fun invoke(): Flow<List<VitalSign>> = repository.getAllVitalSigns()
}

class GetVitalSignsByTypeUseCase constructor(
    private val repository: VitalSignRepository
) {
    operator fun invoke(type: VitalType): Flow<List<VitalSign>> =
        repository.getVitalSignsByType(type)
}

class GetVitalSignsByDateRangeUseCase constructor(
    private val repository: VitalSignRepository
) {
    operator fun invoke(from: LocalDateTime, to: LocalDateTime): Flow<List<VitalSign>> =
        repository.getVitalSignsByDateRange(from, to)
}

class AddVitalSignUseCase constructor(
    private val repository: VitalSignRepository
) {
    suspend operator fun invoke(vitalSign: VitalSign): Long = repository.insertVitalSign(vitalSign)
}

class AddVitalSignIfNotDuplicateUseCase constructor(
    private val repository: VitalSignRepository
) {
    suspend operator fun invoke(vitalSign: VitalSign): Long? = repository.insertIfNotDuplicate(vitalSign)
}

class UpdateVitalSignUseCase constructor(
    private val repository: VitalSignRepository
) {
    suspend operator fun invoke(vitalSign: VitalSign) = repository.updateVitalSign(vitalSign)
}

class GetVitalSignByIdUseCase constructor(
    private val repository: VitalSignRepository
) {
    suspend operator fun invoke(id: Long): VitalSign? = repository.getVitalSignById(id)
}

class DeleteVitalSignUseCase constructor(
    private val repository: VitalSignRepository
) {
    suspend operator fun invoke(vitalSign: VitalSign) = repository.deleteVitalSign(vitalSign)
}
