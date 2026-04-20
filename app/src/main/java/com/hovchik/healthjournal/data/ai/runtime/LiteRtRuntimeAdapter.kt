package com.hovchik.healthjournal.data.ai.runtime

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LiteRtRuntimeAdapter(private val context: Context) : LocalModelRuntime {
    override val runtimeId = "litert"
    override val displayName = "LiteRT (TensorFlow Lite)"

    private var interpreter: Interpreter? = null
    private var loadedModelPath: String? = null

    override fun isAvailable(): Boolean = interpreter != null

    suspend fun loadModel(modelPath: String) {
        withContext(Dispatchers.IO) {
            release()
            val file = File(modelPath)
            if (!file.exists()) throw IllegalStateException("Model file not found: $modelPath")
            val options = Interpreter.Options().apply {
                numThreads = 4
            }
            interpreter = Interpreter(file, options)
            loadedModelPath = modelPath
        }
    }

    override suspend fun runPrompt(prompt: String): String {
        val interp = interpreter ?: throw IllegalStateException("LiteRT model not loaded")
        return withContext(Dispatchers.IO) {
            // TFLite text generation is model-specific.
            // This is a simplified interface — real implementation depends on model architecture.
            // For standard text-generation TFLite models, tokenize → run → detokenize.
            try {
                val inputBytes = prompt.toByteArray(Charsets.UTF_8)
                val inputBuffer = ByteBuffer.allocateDirect(inputBytes.size).apply {
                    order(ByteOrder.nativeOrder())
                    put(inputBytes)
                    rewind()
                }
                val outputBuffer = ByteBuffer.allocateDirect(4096).apply {
                    order(ByteOrder.nativeOrder())
                }
                interp.run(inputBuffer, outputBuffer)
                outputBuffer.rewind()
                val outputBytes = ByteArray(outputBuffer.remaining())
                outputBuffer.get(outputBytes)
                String(outputBytes, Charsets.UTF_8).trim('\u0000')
            } catch (e: Exception) {
                throw RuntimeException("LiteRT inference failed: ${e.message}", e)
            }
        }
    }

    override fun supportsStructuredJson(): Boolean = false
    override fun supportsStreaming(): Boolean = false

    override fun release() {
        try { interpreter?.close() } catch (_: Exception) { }
        interpreter = null
        loadedModelPath = null
    }
}
