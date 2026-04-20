package com.hovchik.healthjournal.data.ai

import android.app.ActivityManager
import android.content.Context
import com.hovchik.healthjournal.data.ai.runtime.LocalModelRuntime
import com.hovchik.healthjournal.domain.model.ai.BenchmarkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAiBenchmarkRunner(private val context: Context) {

    suspend fun run(runtime: LocalModelRuntime, modelId: String): BenchmarkResult =
        withContext(Dispatchers.IO) {
            val memBefore = getUsedMemoryMb()
            val testPrompt = "Summarize the following health data: Patient has blood pressure 120/80, heart rate 72bpm, temperature 36.6C. Provide a brief health assessment."

            val startTime = System.currentTimeMillis()
            val output = try { runtime.runPrompt(testPrompt) } catch (_: Exception) { "" }
            val latencyMs = System.currentTimeMillis() - startTime

            val memAfter = getUsedMemoryMb()
            val outputTokens = output.split(Regex("\\s+")).size.toFloat()
            val tokensPerSecond = if (latencyMs > 0) (outputTokens / latencyMs) * 1000f else 0f

            val rating = when {
                tokensPerSecond >= 15f -> "Excellent"
                tokensPerSecond >= 8f -> "Good"
                tokensPerSecond >= 3f -> "Adequate"
                else -> "Slow"
            }

            BenchmarkResult(
                modelId = modelId,
                latencyMs = latencyMs,
                tokensPerSecond = tokensPerSecond,
                memoryBeforeMb = memBefore,
                memoryAfterMb = memAfter,
                memoryDeltaMb = memAfter - memBefore,
                performanceRating = rating
            )
        }

    private fun getUsedMemoryMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return (memInfo.totalMem - memInfo.availMem) / (1024 * 1024)
    }
}
