package com.llamatik.library.platform

/**
 * JNI bridge to llama.cpp native library.
 * Method signatures must match the native symbols in libllama_jni.so exactly.
 */
object LlamaBridge {

    init {
        System.loadLibrary("llama_jni")
    }

    external fun initGenerateModel(modelPath: String): Boolean
    external fun generate(prompt: String): String
    external fun generateWithContext(system: String, context: String, user: String): String
    external fun generateJson(prompt: String, schema: String?): String
    external fun generateJsonWithContext(system: String, context: String, user: String, schema: String?): String
    external fun initEmbedModel(modelPath: String): Boolean
    external fun embed(input: String): FloatArray
    external fun shutdown()
    external fun nativeCancelGenerate()

    fun updateGenerateParams(
        temperature: Float,
        maxTokens: Int,
        topP: Float,
        topK: Int,
        repeatPenalty: Float
    ) {
        nativeUpdateGenerationParams(temperature, maxTokens, topP, topK, repeatPenalty)
    }

    private external fun nativeUpdateGenerationParams(
        temperature: Float,
        maxTokens: Int,
        topP: Float,
        topK: Int,
        repeatPenalty: Float
    )
}
