package com.healthjournal.data.ai.runtime

import android.content.Context
import android.util.Log
import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaCppRuntimeAdapter(private val context: Context) : LocalModelRuntime {
    override val runtimeId = "llama_cpp"
    override val displayName = "llama.cpp (GGUF)"

    private var modelLoaded = false
    private var loadedModelPath: String? = null

    override fun isAvailable(): Boolean = modelLoaded

    suspend fun loadModel(modelPath: String, nThreads: Int = 4, contextSize: Int = 2048) {
        withContext(Dispatchers.IO) {
            release()
            LlamaBridge.initGenerateModel(modelPath)
            LlamaBridge.updateGenerateParams(
                temperature = 0.7f,
                maxTokens = 1024,
                topP = 0.9f,
                topK = 40,
                repeatPenalty = 1.1f
            )
            modelLoaded = true
            loadedModelPath = modelPath
            Log.d(TAG, "Loaded GGUF model: $modelPath")
        }
    }

    override suspend fun runPrompt(prompt: String): String {
        if (!modelLoaded) throw IllegalStateException("llama.cpp model not loaded")
        return withContext(Dispatchers.IO) {
            LlamaBridge.generate(prompt)
        }
    }

    override fun supportsStructuredJson(): Boolean = false
    override fun supportsStreaming(): Boolean = true

    override fun release() {
        try {
            if (modelLoaded) {
                LlamaBridge.shutdown()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing llama.cpp model", e)
        }
        modelLoaded = false
        loadedModelPath = null
    }

    companion object {
        private const val TAG = "LlamaCppRuntime"
    }
}
