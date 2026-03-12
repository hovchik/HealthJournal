package com.healthjournal.data.ai.runtime

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPipeLlmRuntimeAdapter(private val context: Context) : LocalModelRuntime {
    override val runtimeId = "mediapipe_llm"
    override val displayName = "MediaPipe LLM Inference"

    private var inference: LlmInference? = null
    private var loadedModelPath: String? = null

    override fun isAvailable(): Boolean = inference != null

    suspend fun loadModel(modelPath: String, maxTokens: Int = 1024) {
        withContext(Dispatchers.IO) {
            release()
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(maxTokens)
                .build()
            inference = LlmInference.createFromOptions(context, options)
            loadedModelPath = modelPath
        }
    }

    override suspend fun runPrompt(prompt: String): String {
        val llm = inference ?: throw IllegalStateException("MediaPipe model not loaded")
        return withContext(Dispatchers.IO) {
            llm.generateResponse(prompt)
        }
    }

    override fun supportsStructuredJson(): Boolean = true
    override fun supportsStreaming(): Boolean = true

    override fun release() {
        try { inference?.close() } catch (_: Exception) { }
        inference = null
        loadedModelPath = null
    }
}
