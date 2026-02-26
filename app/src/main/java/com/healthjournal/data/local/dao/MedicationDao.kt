package com.healthjournal.data.local.dao

import androidx.room.*
import com.healthjournal.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY name ASC")
    fun getActive(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Insert
    suspend fun insert(entity: MedicationEntity): Long

    @Update
    suspend fun update(entity: MedicationEntity)

    @Delete
    suspend fun delete(entity: MedicationEntity)
}
