package com.healthjournal.domain.ai

import android.net.Uri
import com.healthjournal.domain.model.ai.GenerationParams
import com.healthjournal.domain.model.ai.LocalModelParams

/**
 * Abstraction for on-device LLM inference.
 * Can be swapped for llama.cpp JNI, ONNX runtime, local HTTP server, etc.
 */
interface LocalInferenceEngine {
    suspend fun loadModel(path: Uri, params: LocalModelParams)
    suspend fun generate(prompt: String, params: GenerationParams): String
    fun isModelLoaded(): Boolean
    fun unloadModel()
}
