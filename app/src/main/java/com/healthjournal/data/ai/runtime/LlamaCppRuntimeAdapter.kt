package com.healthjournal.data.ai.runtime

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.llama.cpp.LLamaAndroid

class LlamaCppRuntimeAdapter(private val context: Context) : LocalModelRuntime {
    override val runtimeId = "llama_cpp"
    override val displayName = "llama.cpp (GGUF)"

    private var llama: LLamaAndroid? = null
    private var loadedModelPath: String? = null

    override fun isAvailable(): Boolean = llama != null

    suspend fun loadModel(modelPath: String, nThreads: Int = 4, contextSize: Int = 2048) {
        withContext(Dispatchers.IO) {
            release()
            val instance = LLamaAndroid.instance()
            instance.load(modelPath, contextSize, nThreads)
            llama = instance
            loadedModelPath = modelPath
            Log.d(TAG, "Loaded GGUF model: $modelPath (ctx=$contextSize, threads=$nThreads)")
        }
    }

    override suspend fun runPrompt(prompt: String): String {
        val instance = llama ?: throw IllegalStateException("llama.cpp model not loaded")
        return withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            instance.send(prompt) { token ->
                sb.append(token)
            }
            sb.toString()
        }
    }

    override fun supportsStructuredJson(): Boolean = false
    override fun supportsStreaming(): Boolean = true

    override fun release() {
        try {
            llama?.unload()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing llama.cpp model", e)
        }
        llama = null
        loadedModelPath = null
    }

    companion object {
        private const val TAG = "LlamaCppRuntime"
    }
}
