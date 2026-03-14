package com.healthjournal.data.ai

import com.healthjournal.data.local.db.LocalAiModelDao
import com.healthjournal.data.local.db.LocalAiModelEntity
import com.healthjournal.domain.model.ai.LocalAiModel
import com.healthjournal.domain.model.ai.ModelInstallState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

class LocalModelManager(
    private val dao: LocalAiModelDao,
    private val aiPreferences: AiPreferences
) {
    fun observeInstalledModels(): Flow<List<LocalAiModel>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeActiveModel(): Flow<LocalAiModel?> =
        dao.observeActive().map { it?.toDomain() }

    suspend fun getInstalledModels(): List<LocalAiModel> =
        dao.getAll().map { it.toDomain() }

    suspend fun getActiveModel(): LocalAiModel? =
        dao.getActive()?.toDomain()

    fun getActiveModelSync(): LocalAiModel? = runBlocking {
        dao.getActive()?.toDomain()
    }

    suspend fun getModel(modelId: String): LocalAiModel? =
        dao.getById(modelId)?.toDomain()

    suspend fun saveModel(model: LocalAiModel) {
        dao.insert(model.toEntity())
    }

    suspend fun setActiveModel(modelId: String) {
        dao.deactivateAll()
        dao.activate(modelId)
        aiPreferences.setActiveModelId(modelId)
    }

    suspend fun deactivateAll() {
        dao.deactivateAll()
        aiPreferences.setActiveModelId(null)
    }

    suspend fun deleteModel(modelId: String) {
        withContext(Dispatchers.IO) {
            val model = dao.getById(modelId)
            model?.localPath?.let { path ->
                val file = File(path)
                file.parentFile?.deleteRecursively()
            }
            dao.delete(modelId)
            val activeId = aiPreferences.getActiveModelId()
            if (activeId == modelId) {
                aiPreferences.setActiveModelId(null)
            }
        }
    }

    suspend fun deleteAllModels() {
        withContext(Dispatchers.IO) {
            dao.getAll().forEach { entity ->
                entity.localPath?.let { path ->
                    File(path).parentFile?.deleteRecursively()
                }
            }
            dao.deleteAll()
            aiPreferences.setActiveModelId(null)
        }
    }

    suspend fun updateInstallState(modelId: String, state: ModelInstallState, localPath: String? = null) {
        val existing = dao.getById(modelId)
        if (existing != null) {
            dao.update(existing.copy(
                installState = state.name,
                localPath = localPath ?: existing.localPath
            ))
        } else {
            // Save from catalog if not yet in DB
            val catalogModel = ModelCatalog.getById(modelId) ?: return
            dao.insert(catalogModel.copy(installState = state).toEntity())
        }
    }

    suspend fun getTotalStorageUsedMb(): Long =
        dao.getByState(ModelInstallState.INSTALLED.name).sumOf { it.sizeMb }

    /**
     * Migrates existing GGUF/GGML models from the incorrect "mediapipe_llm" runtime
     * to "llama_cpp". MediaPipe LLM does not support GGUF files; llama.cpp does.
     * Should be called once at app startup.
     */
    suspend fun migrateGgufRuntimeType() {
        withContext(Dispatchers.IO) {
            val ggufFormats = setOf("gguf", "ggml")
            dao.getAll()
                .filter { it.runtimeType == "mediapipe_llm" && it.fileFormat in ggufFormats }
                .forEach { entity ->
                    dao.update(entity.copy(runtimeType = "llama_cpp"))
                }
        }
    }
}

// ===== Mappers =====

fun LocalAiModelEntity.toDomain() = LocalAiModel(
    modelId = modelId,
    displayName = displayName,
    runtimeType = runtimeType,
    fileFormat = fileFormat,
    quantization = quantization,
    requiredRamMb = requiredRamMb,
    recommendedRamMb = recommendedRamMb,
    sizeMb = sizeMb,
    downloadUrl = downloadUrl,
    localPath = localPath,
    installState = try { ModelInstallState.valueOf(installState) } catch (_: Exception) { ModelInstallState.NOT_INSTALLED },
    checksum = checksum,
    version = version,
    supportsStructuredJson = supportsStructuredJson,
    supportsStreaming = supportsStreaming,
    supportsTextGeneration = supportsTextGeneration,
    isActive = isActive,
    installedAt = installedAt
)

fun LocalAiModel.toEntity() = LocalAiModelEntity(
    modelId = modelId,
    displayName = displayName,
    runtimeType = runtimeType,
    fileFormat = fileFormat,
    quantization = quantization,
    requiredRamMb = requiredRamMb,
    recommendedRamMb = recommendedRamMb,
    sizeMb = sizeMb,
    downloadUrl = downloadUrl,
    localPath = localPath,
    installState = installState.name,
    checksum = checksum,
    version = version,
    supportsStructuredJson = supportsStructuredJson,
    supportsStreaming = supportsStreaming,
    supportsTextGeneration = supportsTextGeneration,
    isActive = isActive,
    installedAt = installedAt
)
