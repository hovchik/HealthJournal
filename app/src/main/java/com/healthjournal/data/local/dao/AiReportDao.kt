package com.healthjournal.data.local.dao

import androidx.room.*
import com.healthjournal.data.local.entity.AiReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiReportDao {
    @Query("SELECT * FROM ai_reports ORDER BY generatedAt DESC")
    fun getAll(): Flow<List<AiReportEntity>>

    @Query("SELECT * FROM ai_reports WHERE id = :id")
    suspend fun getById(id: Long): AiReportEntity?

    @Insert
    suspend fun insert(entity: AiReportEntity): Long

    @Delete
    suspend fun delete(entity: AiReportEntity)
}
