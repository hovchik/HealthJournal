package com.healthjournal.data.ai.provider

import android.util.Log
import com.healthjournal.R
import com.healthjournal.data.ai.LocalModelManager
import com.healthjournal.data.ai.runtime.LiteRtRuntimeAdapter
import com.healthjournal.data.ai.runtime.LlamaCppRuntimeAdapter
import com.healthjournal.data.ai.runtime.LocalModelRuntime
import com.healthjournal.data.ai.LocalAnalysisEngine
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class CustomLocalModelProvider(
    private val modelManager: LocalModelManager,
    private val liteRtRuntime: LiteRtRuntimeAdapter,
    private val llamaCppRuntime: LlamaCppRuntimeAdapter
) : AiProvider {

    companion object {
        private const val TAG = "CustomLocalModelProvider"
    }

    override val id = AiProviderId.LOCAL
    override val displayNameResId = R.string.ai_provider_local

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val model = modelManager.getActiveModel()
        if (model == null) {
            return AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val runtime = resolveRuntime(model, config.localAiConfig)
            val prompt = PromptTemplate.buildSummaryPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = runtime.runPrompt(fullPrompt)
            AiTextResult(text = result, providerId = id, modelUsed = model.displayName)
        } catch (e: Exception) {
            Log.e(TAG, "Model '${model.displayName}' failed, falling back to rule-based analysis", e)
            val fallbackText = LocalAnalysisEngine.buildLocalSummary(input)
            val warning = buildModelFailureWarning(model.displayName, input.outputLanguage)
            AiTextResult(
                text = "$warning\n\n$fallbackText",
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val model = modelManager.getActiveModel()
        if (model == null) {
            return AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val runtime = resolveRuntime(model, config.localAiConfig)
            val prompt = PromptTemplate.buildPatternPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = runtime.runPrompt(fullPrompt)
            AiFlagsResult(text = result, providerId = id, modelUsed = model.displayName)
        } catch (e: Exception) {
            Log.e(TAG, "Model '${model.displayName}' failed, falling back to rule-based analysis", e)
            val fallbackText = LocalAnalysisEngine.buildLocalPatternAnalysis(input)
            val warning = buildModelFailureWarning(model.displayName, input.outputLanguage)
            AiFlagsResult(
                text = "$warning\n\n$fallbackText",
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val model = modelManager.getActiveModelSync()
        return if (model != null) {
            ValidationResult(true, "Active model: ${model.displayName}")
        } else {
            ValidationResult(true, "No model downloaded — using rule-based analysis")
        }
    }

    override fun isOnlineRequired(): Boolean = false

    private fun buildModelFailureWarning(modelName: String, lang: String): String {
        return when {
            lang.startsWith("ru") ->
                "\u26A0 \u041C\u043E\u0434\u0435\u043B\u044C \u00AB$modelName\u00BB \u043D\u0435 \u0441\u043C\u043E\u0433\u043B\u0430 \u0437\u0430\u043F\u0443\u0441\u0442\u0438\u0442\u044C\u0441\u044F \u043D\u0430 \u044D\u0442\u043E\u043C \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0435. \u041F\u043E\u043F\u0440\u043E\u0431\u0443\u0439\u0442\u0435 \u0434\u0440\u0443\u0433\u0443\u044E \u043C\u043E\u0434\u0435\u043B\u044C \u0438\u043B\u0438 \u043E\u0431\u043B\u0430\u0447\u043D\u044B\u0439 AI. \u041D\u0438\u0436\u0435 \u2014 \u0430\u043D\u0430\u043B\u0438\u0437 \u043D\u0430 \u043E\u0441\u043D\u043E\u0432\u0435 \u043F\u0440\u0430\u0432\u0438\u043B."
            lang.startsWith("es") ->
                "\u26A0 El modelo \u00AB$modelName\u00BB no pudo ejecutarse en este dispositivo. Pruebe otro modelo o AI en la nube. A continuaci\u00F3n se muestra el an\u00E1lisis basado en reglas."
            lang.startsWith("zh") ->
                "\u26A0 \u6A21\u578B\u201C$modelName\u201D\u65E0\u6CD5\u5728\u6B64\u8BBE\u5907\u4E0A\u8FD0\u884C\u3002\u8BF7\u5C1D\u8BD5\u5176\u4ED6\u6A21\u578B\u6216\u4E91\u7AEF AI\u3002\u4EE5\u4E0B\u662F\u57FA\u4E8E\u89C4\u5219\u7684\u5206\u6790\u3002"
            lang.startsWith("hy") ->
                "\u26A0 \u00AB$modelName\u00BB \u0574\u0578\u0564\u0565\u056C\u0568 \u0579\u056F\u0561\u0580\u0578\u0572\u0561\u0581\u0561\u057E \u0563\u0578\u0580\u056E\u0565\u056C \u0561\u0575\u057D \u057D\u0561\u0580\u0584\u0578\u0582\u0574\u0589 \u0553\u0578\u0580\u0571\u0565\u0584 \u0561\u0575\u056C \u0574\u0578\u0564\u0565\u056C \u056F\u0561\u0574 \u0561\u0574\u057A\u0561\u0575\u056B\u0576 AI\u0589 \u054D\u057F\u0578\u0580\u0587 \u0576\u0565\u0580\u056F\u0561\u0575\u0561\u0581\u057E\u0561\u056E \u0567 \u056F\u0561\u0576\u0578\u0576\u0576\u0565\u0580\u056B \u057E\u0580\u0561 \u057E\u0565\u0580\u056C\u0578\u0582\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0568\u0589"
            else ->
                "\u26A0 Model '$modelName' could not run on this device. Try a different model or cloud AI. Below is rule-based analysis."
        }
    }

    private suspend fun resolveRuntime(model: LocalAiModel, localConfig: LocalAiConfig): LocalModelRuntime {
        val path = model.localPath ?: throw IllegalStateException("Model has no local path")
        val effectiveRuntime = when (model.runtimeType) {
            "llama_cpp", "litert" -> model.runtimeType
            "mediapipe_llm" -> if (model.fileFormat == "tflite") "litert" else "llama_cpp"
            else -> throw IllegalArgumentException("Unsupported runtime type: '${model.runtimeType}'. Supported: llama_cpp, litert")
        }
        return when (effectiveRuntime) {
            "llama_cpp" -> {
                if (!llamaCppRuntime.isAvailable()) {
                    llamaCppRuntime.loadModel(path, contextSize = localConfig.contextSize)
                }
                llamaCppRuntime
            }
            "litert" -> {
                if (!liteRtRuntime.isAvailable()) {
                    liteRtRuntime.loadModel(path)
                }
                liteRtRuntime
            }
            else -> throw IllegalArgumentException("Unsupported runtime type: '$effectiveRuntime'. Supported: llama_cpp, litert")
        }
    }
}
