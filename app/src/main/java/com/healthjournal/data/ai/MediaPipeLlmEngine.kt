package com.healthjournal.data.ai

import android.net.Uri
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.healthjournal.domain.ai.LocalInferenceEngine
import com.healthjournal.domain.model.ai.GenerationParams
import com.healthjournal.domain.model.ai.LocalModelParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device LLM inference engine using Google MediaPipe LLM Inference API.
 * Supports .task and .bin model formats from Google AI Edge / HuggingFace.
 */
class MediaPipeLlmEngine(
    private val modelRepository: ModelRepository
) : LocalInferenceEngine {

    private var inference: LlmInference? = null
    private var loadedModelPath: String? = null

    override suspend fun loadModel(path: Uri, params: LocalModelParams) {
        withContext(Dispatchers.IO) {
            unloadModel()

            val modelPath = path.path ?: path.toString()
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(params.maxTokens)
                .setTemperature(params.temperature)
                .build()

            inference = LlmInference.createFromOptions(
                modelRepository.getModelFile(modelRepository.availableModels.first()).parentFile!!.let {
                    // Use the model path directly
                    null
                } ?: throw IllegalStateException("Cannot create context"),
                options
            )
            loadedModelPath = modelPath
        }
    }

    /**
     * Load model directly from a file path (preferred method).
     */
    suspend fun loadModelFromPath(modelPath: String, params: LocalModelParams, context: android.content.Context) {
        withContext(Dispatchers.IO) {
            unloadModel()

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(params.maxTokens)
                .setTemperature(params.temperature)
                .build()

            inference = LlmInference.createFromOptions(context, options)
            loadedModelPath = modelPath
        }
    }

    override suspend fun generate(prompt: String, params: GenerationParams): String {
        val llm = inference ?: throw IllegalStateException("Model not loaded")
        return withContext(Dispatchers.IO) {
            llm.generateResponse(prompt)
        }
    }

    override fun isModelLoaded(): Boolean = inference != null

    override fun unloadModel() {
        try {
            inference?.close()
        } catch (_: Exception) { }
        inference = null
        loadedModelPath = null
    }
}
