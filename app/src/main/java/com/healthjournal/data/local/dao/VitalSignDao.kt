package com.healthjournal.data.local.dao

import androidx.room.*
import com.healthjournal.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalSignDao {
    @Query("SELECT * FROM vital_signs ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE type = :type ORDER BY recordedAt DESC")
    fun getByType(type: String): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE recordedAt BETWEEN :from AND :to ORDER BY recordedAt DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE id = :id")
    suspend fun getById(id: Long): VitalSignEntity?

    @Insert
    suspend fun insert(entity: VitalSignEntity): Long

    @Update
    suspend fun update(entity: VitalSignEntity)

    @Delete
    suspend fun delete(entity: VitalSignEntity)
}
