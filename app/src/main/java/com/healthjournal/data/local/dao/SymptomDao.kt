package com.healthjournal.data.local.dao

import androidx.room.*
import com.healthjournal.data.local.entity.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE recordedAt BETWEEN :from AND :to ORDER BY recordedAt DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE id = :id")
    suspend fun getById(id: Long): SymptomEntity?

    @Insert
    suspend fun insert(entity: SymptomEntity): Long

    @Update
    suspend fun update(entity: SymptomEntity)

    @Delete
    suspend fun delete(entity: SymptomEntity)
}
