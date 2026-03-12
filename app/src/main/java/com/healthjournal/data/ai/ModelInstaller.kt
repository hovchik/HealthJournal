package com.healthjournal.data.ai

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class ModelInstaller(
    private val context: Context,
    private val localModelManager: LocalModelManager,
    private val compatibilityValidator: ModelCompatibilityValidator
) {
    private val modelsDir = File(context.filesDir, "local_models").apply { mkdirs() }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .header("User-Agent", "HealthJournal-Android/1.0")
                .build())
        }
        .build()

    private val _installProgress = MutableStateFlow<InstallProgress?>(null)
    val installProgress: StateFlow<InstallProgress?> = _installProgress.asStateFlow()

    fun getModelDir(modelId: String): File = File(modelsDir, modelId).apply { mkdirs() }

    fun getModelFile(modelId: String, format: String): File =
        File(getModelDir(modelId), "model.$format")

    suspend fun downloadModel(model: LocalAiModel, url: String) {
        require(url.isNotBlank()) { "Download URL is blank" }

        val report = compatibilityValidator.validate(model)
        if (!report.isCompatible) {
            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.FAILED,
                errorMessage = "Incompatible: ${report.issues.joinToString("; ")}")
            return
        }

        _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, 0)
        localModelManager.updateInstallState(model.modelId, ModelInstallState.DOWNLOADING)

        withContext(Dispatchers.IO) {
            try {
                val modelDir = getModelDir(model.modelId)
                val targetFile = getModelFile(model.modelId, model.fileFormat)
                val tmpFile = File(modelDir, "model.${model.fileFormat}.tmp")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    fail(model.modelId, "Download failed: HTTP ${response.code}")
                    return@withContext
                }

                val body = response.body ?: run {
                    fail(model.modelId, "Empty response body")
                    return@withContext
                }

                val totalBytes = body.contentLength()
                tmpFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Long = 0
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val percent = if (totalBytes > 0) ((bytesRead * 100) / totalBytes).toInt() else 0
                            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, percent)
                        }
                    }
                }

                _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLING, 100)
                localModelManager.updateInstallState(model.modelId, ModelInstallState.INSTALLING)

                tmpFile.renameTo(targetFile)
                val checksum = computeChecksum(targetFile)

                val installed = model.copy(
                    localPath = targetFile.absolutePath,
                    installState = ModelInstallState.INSTALLED,
                    checksum = checksum,
                    installedAt = System.currentTimeMillis()
                )
                localModelManager.saveModel(installed)

                _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLED, 100)
            } catch (e: Exception) {
                fail(model.modelId, e.message ?: "Unknown download error")
            }
        }
    }

    suspend fun importFromUri(model: LocalAiModel, uri: Uri) {
        _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, 0)
        localModelManager.updateInstallState(model.modelId, ModelInstallState.DOWNLOADING)

        withContext(Dispatchers.IO) {
            try {
                val modelDir = getModelDir(model.modelId)
                val targetFile = getModelFile(model.modelId, model.fileFormat)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Long = 0
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val percent = if (model.sizeMb > 0) {
                                ((bytesRead * 100) / (model.sizeMb * 1024 * 1024)).toInt().coerceAtMost(99)
                            } else 50
                            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, percent)
                        }
                    }
                } ?: run {
                    fail(model.modelId, "Cannot open file")
                    return@withContext
                }

                val checksum = computeChecksum(targetFile)
                val installed = model.copy(
                    localPath = targetFile.absolutePath,
                    installState = ModelInstallState.INSTALLED,
                    checksum = checksum,
                    installedAt = System.currentTimeMillis()
                )
                localModelManager.saveModel(installed)
                _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLED, 100)
            } catch (e: Exception) {
                fail(model.modelId, e.message ?: "Import failed")
            }
        }
    }

    suspend fun registerFromPath(model: LocalAiModel, path: String) {
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) {
                fail(model.modelId, "File does not exist: $path")
                return@withContext
            }
            val report = compatibilityValidator.validate(model)
            if (!report.isCompatible) {
                fail(model.modelId, "Incompatible: ${report.issues.joinToString("; ")}")
                return@withContext
            }
            val checksum = computeChecksum(file)
            val registered = model.copy(
                localPath = path,
                installState = ModelInstallState.INSTALLED,
                checksum = checksum,
                installedAt = System.currentTimeMillis()
            )
            localModelManager.saveModel(registered)
            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLED, 100)
        }
    }

    suspend fun scanForModels(): Int {
        var found = 0
        withContext(Dispatchers.IO) {
            // 1. Scan internal local_models/ directory
            modelsDir.listFiles()?.forEach { dir ->
                if (!dir.isDirectory) return@forEach
                val modelId = dir.name
                val existing = localModelManager.getModel(modelId)
                if (existing?.installState == ModelInstallState.INSTALLED) return@forEach

                val modelFile = dir.listFiles()?.firstOrNull {
                    it.name.endsWith(".bin") || it.name.endsWith(".tflite") || it.name.endsWith(".gguf")
                } ?: return@forEach

                val catalogModel = ModelCatalog.getById(modelId)
                if (catalogModel != null) {
                    val registered = catalogModel.copy(
                        localPath = modelFile.absolutePath,
                        installState = ModelInstallState.INSTALLED,
                        checksum = computeChecksum(modelFile),
                        installedAt = modelFile.lastModified()
                    )
                    localModelManager.saveModel(registered)
                    found++
                }
            }

            // 2. Scan Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir?.exists() == true) {
                downloadsDir.listFiles()?.filter { file ->
                    file.isFile && file.length() > 10 * 1024 * 1024 &&
                    (file.name.endsWith(".gguf") || file.name.endsWith(".bin") || file.name.endsWith(".tflite"))
                }?.forEach { file ->
                    val stableId = "imported-${file.nameWithoutExtension.lowercase().replace(Regex("[^a-z0-9]"), "-")}"
                    val existing = localModelManager.getModel(stableId)
                    if (existing != null) return@forEach

                    val catalogMatch = matchCatalogModel(file.name)
                    val format = file.extension
                    val quantization = extractQuantization(file.name)

                    val model = catalogMatch?.copy(
                        modelId = stableId,
                        localPath = file.absolutePath,
                        installState = ModelInstallState.INSTALLED,
                        checksum = computeChecksum(file),
                        installedAt = file.lastModified()
                    ) ?: LocalAiModel(
                        modelId = stableId,
                        displayName = file.nameWithoutExtension,
                        runtimeType = if (format == "tflite") "litert" else "mediapipe_llm",
                        fileFormat = format,
                        quantization = quantization,
                        requiredRamMb = 2000,
                        recommendedRamMb = 4000,
                        sizeMb = file.length() / (1024 * 1024),
                        localPath = file.absolutePath,
                        installState = ModelInstallState.INSTALLED,
                        checksum = computeChecksum(file),
                        version = "1.0",
                        supportsStructuredJson = false,
                        supportsStreaming = false,
                        supportsTextGeneration = true,
                        installedAt = file.lastModified()
                    )
                    localModelManager.saveModel(model)
                    found++
                }
            }
        }
        return found
    }

    private fun matchCatalogModel(filename: String): LocalAiModel? {
        val lower = filename.lowercase()
        return ModelCatalog.availableModels.firstOrNull { catalog ->
            val keywords = catalog.modelId.split("-", ".", "_")
            keywords.count { kw -> lower.contains(kw) } >= 2
        }
    }

    private fun extractQuantization(filename: String): String? {
        val lower = filename.lowercase()
        val patterns = listOf("q4_k_m", "q4_k_s", "q4_0", "q4_1", "q5_k_m", "q5_0", "q8_0", "q6_k", "q3_k_m", "q2_k")
        return patterns.firstOrNull { lower.contains(it) }?.uppercase()
    }

    private fun computeChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private suspend fun fail(modelId: String, message: String) {
        localModelManager.updateInstallState(modelId, ModelInstallState.FAILED)
        _installProgress.value = InstallProgress(modelId, ModelInstallState.FAILED, errorMessage = message)
    }
}
