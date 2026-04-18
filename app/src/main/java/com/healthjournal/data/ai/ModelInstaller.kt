package com.healthjournal.data.ai

import android.content.Context
import android.net.Uri
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

class ModelInstaller(
    private val context: Context,
    private val localModelManager: LocalModelManager,
    private val compatibilityValidator: ModelCompatibilityValidator,
    private val downloadManager: ModelDownloadManager
) {
    private val modelsDir = File(context.filesDir, "local_models").apply { mkdirs() }

    private val _installProgress = MutableStateFlow<InstallProgress?>(null)
    val installProgress: StateFlow<InstallProgress?> = _installProgress.asStateFlow()

    private val _scanProgress = MutableStateFlow<ScanProgress?>(null)
    val scanProgress: StateFlow<ScanProgress?> = _scanProgress.asStateFlow()

    private var progressForwardJob: kotlinx.coroutines.Job? = null

    companion object {
        /** LLM model file extensions supported for scanning (llama.cpp compatible) */
        private val MODEL_EXTENSIONS = setOf(
            "gguf",         // GGUF — llama.cpp modern format (dominant)
            "ggml",         // GGML — llama.cpp legacy format
            "bin"           // BIN — generic weights (llama.cpp compatible)
        )

        /** Minimum file size to consider as a model (10 MB) */
        private const val MIN_MODEL_SIZE_BYTES = 10L * 1024 * 1024

        /** Maximum scan depth for recursive directory traversal */
        private const val MAX_SCAN_DEPTH = 4

        /** Directories to skip during recursive scan */
        private val SKIP_DIR_NAMES = setOf(
            "Android", "DCIM", "Pictures", "Movies", "Music", "Ringtones",
            "Alarms", "Notifications", "Podcasts", "cache", ".thumbnails",
            ".trash", "thumbnails", "LOST.DIR"
        )

        /** Map file extension to runtime type */
        fun inferRuntimeType(extension: String): String = "llama_cpp"
    }

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

        // Forward progress from download manager to our installProgress
        val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
        progressForwardJob?.cancel()
        progressForwardJob = scope.launch {
            downloadManager.progress.collect { progress ->
                if (progress != null) _installProgress.value = progress
            }
        }

        try {
            val targetFile = getModelFile(model.modelId, model.fileFormat)
            val result = downloadManager.download(model.modelId, url, targetFile, scope)

            result.onSuccess { file ->
                _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLING, 100,
                    bytesDownloaded = file.length(), totalBytes = file.length())
                localModelManager.updateInstallState(model.modelId, ModelInstallState.INSTALLING)

                val checksum = withContext(Dispatchers.IO) { computeChecksum(file) }
                val installed = model.copy(
                    localPath = file.absolutePath,
                    installState = ModelInstallState.INSTALLED,
                    checksum = checksum,
                    installedAt = System.currentTimeMillis()
                )
                localModelManager.saveModel(installed)
                localModelManager.setActiveModel(model.modelId)
                _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLED, 100,
                    bytesDownloaded = file.length(), totalBytes = file.length())
            }.onFailure { e ->
                if (e is ModelDownloadManager.PauseException) {
                    // Keep current paused progress — don't overwrite
                } else {
                    fail(model.modelId, e.message ?: "Unknown download error")
                }
            }
        } catch (e: Exception) {
            fail(model.modelId, e.message ?: "Unknown download error")
        } finally {
            progressForwardJob?.cancel()
            scope.cancel()
        }
    }

    fun pauseDownload() {
        downloadManager.pause()
    }

    fun isDownloadPaused(): Boolean = downloadManager.isPaused()

    suspend fun importFromUri(model: LocalAiModel, uri: Uri) {
        _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, 0)
        localModelManager.updateInstallState(model.modelId, ModelInstallState.DOWNLOADING)

        withContext(Dispatchers.IO) {
            try {
                val targetFile = getModelFile(model.modelId, model.fileFormat)
                val totalEstimate = model.sizeMb * 1024 * 1024

                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(65536)
                        var bytesRead: Long = 0
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val percent = if (totalEstimate > 0) {
                                ((bytesRead * 100) / totalEstimate).toInt().coerceAtMost(99)
                            } else 50
                            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.DOWNLOADING, percent,
                                bytesDownloaded = bytesRead, totalBytes = totalEstimate)
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
                localModelManager.setActiveModel(model.modelId)
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
            localModelManager.setActiveModel(model.modelId)
            _installProgress.value = InstallProgress(model.modelId, ModelInstallState.INSTALLED, 100)
        }
    }

    suspend fun scanForModels(): Int {
        var found = 0
        withContext(Dispatchers.IO) {
            // Scan only app-owned directories so the feature works without
            // MANAGE_EXTERNAL_STORAGE or READ_EXTERNAL_STORAGE, which Google
            // Play restricts and rejects for general-purpose apps. Users who
            // keep models elsewhere can still import them via the SAF file
            // picker (importFromUri) which needs no permission.
            val scanDirs = buildList {
                add("local_models" to modelsDir)
                add("App Internal" to context.filesDir)
                context.getExternalFilesDir(null)
                    ?.let { add("App External" to it) }
                context.getExternalFilesDirs(null)?.forEachIndexed { index, dir ->
                    if (index > 0 && dir != null && dir.exists()) {
                        add("App External ${index}" to dir)
                    }
                }
            }.filter { it.second.exists() }
                .distinctBy { it.second.absolutePath }

            val totalFolders = scanDirs.size
            _scanProgress.value = ScanProgress(
                isScanning = true,
                totalFolders = totalFolders,
                modelsFound = 0
            )

            scanDirs.forEachIndexed { index, (label, dir) ->
                _scanProgress.value = ScanProgress(
                    isScanning = true,
                    currentFolder = label,
                    foldersScanned = index,
                    totalFolders = totalFolders,
                    modelsFound = found
                )

                if (dir == modelsDir) {
                    // Scan internal local_models/ directory (existing catalog matching)
                    found += scanInternalModelsDir(dir)
                } else {
                    // Deep recursive scan for all supported model formats
                    found += scanDirectoryRecursive(dir, 0)
                }
            }

            _scanProgress.value = ScanProgress(
                isScanning = false,
                foldersScanned = totalFolders,
                totalFolders = totalFolders,
                modelsFound = found
            )
        }
        return found
    }

    private suspend fun scanInternalModelsDir(dir: File): Int {
        var found = 0
        dir.listFiles()?.forEach { subDir ->
            if (!subDir.isDirectory) return@forEach
            val modelId = subDir.name
            val existing = localModelManager.getModel(modelId)
            if (existing?.installState == ModelInstallState.INSTALLED) return@forEach

            val modelFile = subDir.listFiles()?.firstOrNull {
                isModelFile(it.name)
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
            } else if (isModelFile(modelFile.name) && modelFile.length() > MIN_MODEL_SIZE_BYTES) {
                found += registerScannedFile(modelFile)
            }
        }
        return found
    }

    private suspend fun scanDirectoryRecursive(dir: File, depth: Int): Int {
        if (depth > MAX_SCAN_DEPTH) return 0

        var found = 0
        try {
            val files = dir.listFiles() ?: return 0
            for (file in files) {
                if (file.isFile && isModelFile(file.name) && file.length() > MIN_MODEL_SIZE_BYTES) {
                    found += registerScannedFile(file)
                } else if (file.isDirectory && file.name !in SKIP_DIR_NAMES && !file.name.startsWith(".")) {
                    found += scanDirectoryRecursive(file, depth + 1)
                }
            }
        } catch (_: SecurityException) {
            // Skip directories we don't have permission to access
        } catch (_: Exception) {
            // Skip unreadable directories
        }
        return found
    }

    private fun isModelFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in MODEL_EXTENSIONS
    }

    private suspend fun registerScannedFile(file: File): Int {
        val stableId = "imported-${file.nameWithoutExtension.lowercase().replace(Regex("[^a-z0-9]"), "-")}"
        val existing = localModelManager.getModel(stableId)
        if (existing != null) return 0

        val catalogMatch = matchCatalogModel(file.name)
        val format = file.extension.lowercase()
        val quantization = extractQuantization(file.name)
        val runtimeType = inferRuntimeType(format)

        val model = catalogMatch?.copy(
            modelId = stableId,
            localPath = file.absolutePath,
            fileFormat = format,
            runtimeType = runtimeType,
            installState = ModelInstallState.INSTALLED,
            checksum = computeChecksum(file),
            installedAt = file.lastModified()
        ) ?: LocalAiModel(
            modelId = stableId,
            displayName = buildDisplayName(file.nameWithoutExtension, format),
            runtimeType = runtimeType,
            fileFormat = format,
            quantization = quantization,
            requiredRamMb = estimateRequiredRam(file.length()),
            recommendedRamMb = estimateRecommendedRam(file.length()),
            sizeMb = file.length() / (1024 * 1024),
            localPath = file.absolutePath,
            installState = ModelInstallState.INSTALLED,
            checksum = computeChecksum(file),
            version = "1.0",
            supportsStructuredJson = false,
            supportsStreaming = format in setOf("gguf", "ggml", "bin"),
            supportsTextGeneration = true,
            installedAt = file.lastModified()
        )
        localModelManager.saveModel(model)
        return 1
    }

    private fun buildDisplayName(nameWithoutExtension: String, format: String): String {
        val clean = nameWithoutExtension
            .replace(Regex("[_\\-]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val formatLabel = format.uppercase()
        return "$clean ($formatLabel)"
    }

    private fun estimateRequiredRam(fileSizeBytes: Long): Int {
        val sizeMb = fileSizeBytes / (1024 * 1024)
        // Model typically needs ~1.2x its file size in RAM
        return (sizeMb * 1.2).toInt().coerceAtLeast(1000)
    }

    private fun estimateRecommendedRam(fileSizeBytes: Long): Int {
        val sizeMb = fileSizeBytes / (1024 * 1024)
        // Recommended is ~2x file size for comfortable operation
        return (sizeMb * 2.0).toInt().coerceAtLeast(2000)
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
        val patterns = listOf(
            "q4_k_m", "q4_k_s", "q4_k_l",
            "q4_0", "q4_1",
            "q5_k_m", "q5_k_s", "q5_0", "q5_1",
            "q6_k",
            "q8_0", "q8_1",
            "q3_k_m", "q3_k_s", "q3_k_l",
            "q2_k",
            "f16", "f32",
            "int4", "int8",
            "w4a16", "w8a8", "w4a8"
        )
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
