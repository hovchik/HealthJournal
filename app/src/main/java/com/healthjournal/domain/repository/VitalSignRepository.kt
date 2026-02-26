package com.healthjournal.domain.repository

import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface VitalSignRepository {
    fun getAllVitalSigns(): Flow<List<VitalSign>>
    fun getVitalSignsByType(type: VitalType): Flow<List<VitalSign>>
    fun getVitalSignsByDateRange(from: LocalDateTime, to: LocalDateTime): Flow<List<VitalSign>>
    suspend fun getVitalSignById(id: Long): VitalSign?
    suspend fun insertVitalSign(vitalSign: VitalSign): Long
    suspend fun updateVitalSign(vitalSign: VitalSign)
    suspend fun deleteVitalSign(vitalSign: VitalSign)
}
