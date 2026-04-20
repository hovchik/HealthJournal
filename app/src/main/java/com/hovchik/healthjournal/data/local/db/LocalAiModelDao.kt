package com.hovchik.healthjournal.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalAiModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: LocalAiModelEntity)

    @Update
    suspend fun update(model: LocalAiModelEntity)

    @Query("SELECT * FROM local_ai_models WHERE model_id = :modelId")
    suspend fun getById(modelId: String): LocalAiModelEntity?

    @Query("SELECT * FROM local_ai_models ORDER BY display_name ASC")
    fun observeAll(): Flow<List<LocalAiModelEntity>>

    @Query("SELECT * FROM local_ai_models ORDER BY display_name ASC")
    suspend fun getAll(): List<LocalAiModelEntity>

    @Query("SELECT * FROM local_ai_models WHERE is_active = 1 LIMIT 1")
    suspend fun getActive(): LocalAiModelEntity?

    @Query("SELECT * FROM local_ai_models WHERE is_active = 1 LIMIT 1")
    fun observeActive(): Flow<LocalAiModelEntity?>

    @Query("SELECT * FROM local_ai_models WHERE install_state = :state")
    suspend fun getByState(state: String): List<LocalAiModelEntity>

    @Query("UPDATE local_ai_models SET is_active = 0")
    suspend fun deactivateAll()

    @Query("UPDATE local_ai_models SET is_active = 1 WHERE model_id = :modelId")
    suspend fun activate(modelId: String)

    @Query("DELETE FROM local_ai_models WHERE model_id = :modelId")
    suspend fun delete(modelId: String)

    @Query("DELETE FROM local_ai_models")
    suspend fun deleteAll()
}
