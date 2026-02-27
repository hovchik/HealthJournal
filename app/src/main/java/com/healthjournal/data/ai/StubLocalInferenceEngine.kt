package com.healthjournal.data.ai

import android.net.Uri
import com.healthjournal.domain.ai.LocalInferenceEngine
import com.healthjournal.domain.model.ai.GenerationParams
import com.healthjournal.domain.model.ai.LocalModelParams

/**
 * Stub implementation of LocalInferenceEngine.
 * Replace with llama.cpp JNI, ONNX runtime, or local HTTP server integration.
 */
class StubLocalInferenceEngine : LocalInferenceEngine {

    private var loaded = false

    override suspend fun loadModel(path: Uri, params: LocalModelParams) {
        // Stub: simulate model loading
        loaded = true
    }

    override suspend fun generate(prompt: String, params: GenerationParams): String {
        if (!loaded) throw IllegalStateException("Model not loaded")
        return "[Local AI stub] Analysis is not available. " +
            "Please configure a real local inference engine (llama.cpp, ONNX, etc.) " +
            "to enable on-device AI analysis."
    }

    override fun isModelLoaded(): Boolean = loaded

    override fun unloadModel() {
        loaded = false
    }
}
