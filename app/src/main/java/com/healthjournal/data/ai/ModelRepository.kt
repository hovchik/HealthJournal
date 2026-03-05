package com.healthjournal.data.ai

import android.content.Context
import com.healthjournal.domain.model.ai.DownloadableModel
import com.healthjournal.domain.model.ai.ModelDownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Manages downloading and storing on-device LLM models.
 * Models are stored in the app's internal files directory under "models/".
 */
class ModelRepository(
    private val context: Context
) {
    private val modelsDir = File(context.filesDir, "models").apply { mkdirs() }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    /**
     * Curated list of small models suitable for mobile health analysis.
     * These are MediaPipe-compatible .bin models from Google AI Edge / HuggingFace.
     */
    val availableModels: List<DownloadableModel> = listOf(
        DownloadableModel(
            id = "gemma3-1b",
            name = "Gemma 3 1B",
            description = "Google Gemma 3 1B — fast, lightweight, good for summaries",
            sizeBytes = 550_000_000L,
            url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task",
            fileName = "gemma3-1b.task"
        ),
        DownloadableModel(
            id = "gemma2-2b",
            name = "Gemma 2 2B",
            description = "Google Gemma 2 2B — balanced quality and speed",
            sizeBytes = 1_400_000_000L,
            url = "https://huggingface.co/litert-community/Gemma2-2B-IT/resolve/main/gemma2-2b-it-gpu-int8.bin",
            fileName = "gemma2-2b.bin"
        )
    )

    fun getModelFile(model: DownloadableModel): File = File(modelsDir, model.fileName)

    fun isModelDownloaded(model: DownloadableModel): Boolean =
        getModelFile(model).exists() && getModelFile(model).length() > 0

    fun getDownloadedModel(): DownloadableModel? =
        availableModels.firstOrNull { isModelDownloaded(it) }

    fun getDownloadedModelPath(): String? =
        getDownloadedModel()?.let { getModelFile(it).absolutePath }

    suspend fun downloadModel(model: DownloadableModel) {
        _downloadState.value = ModelDownloadState.Downloading(0f)
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(model.url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _downloadState.value = ModelDownloadState.Error("Download failed: HTTP ${response.code}")
                    return@withContext
                }

                val body = response.body ?: run {
                    _downloadState.value = ModelDownloadState.Error("Empty response")
                    return@withContext
                }

                val totalBytes = body.contentLength()
                val file = getModelFile(model)
                val tmpFile = File(modelsDir, "${model.fileName}.tmp")

                tmpFile.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Long = 0
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            if (totalBytes > 0) {
                                _downloadState.value = ModelDownloadState.Downloading(
                                    bytesRead.toFloat() / totalBytes
                                )
                            }
                        }
                    }
                }

                tmpFile.renameTo(file)
                _downloadState.value = ModelDownloadState.Downloaded
            } catch (e: Exception) {
                _downloadState.value = ModelDownloadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteModel(model: DownloadableModel) {
        getModelFile(model).delete()
        File(modelsDir, "${model.fileName}.tmp").delete()
        _downloadState.value = ModelDownloadState.NotDownloaded
    }

    fun refreshState() {
        val downloaded = getDownloadedModel()
        _downloadState.value = if (downloaded != null) {
            ModelDownloadState.Downloaded
        } else {
            ModelDownloadState.NotDownloaded
        }
    }
}
