package com.healthjournal.data.ai

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.healthjournal.domain.model.ai.*

class DeviceAiCapabilityDetector(private val context: Context) {

    fun detect(): DeviceCapabilityResult {
        val totalRamMb = getTotalRamMb()
        val availableStorageMb = getAvailableStorageMb()
        val aiCoreAvailable = isPackageInstalled("com.google.android.aicore")
        val aiCoreVersion = getPackageVersion("com.google.android.aicore")
        val mlKitAvailable = isPackageInstalled("com.google.android.gms") && Build.VERSION.SDK_INT >= 24
        val abis = Build.SUPPORTED_ABIS.toList()
        val ramTier = when {
            totalRamMb >= RamTier.VERY_HIGH.minMb -> RamTier.VERY_HIGH
            totalRamMb >= RamTier.HIGH.minMb -> RamTier.HIGH
            totalRamMb >= RamTier.MEDIUM.minMb -> RamTier.MEDIUM
            else -> RamTier.LOW
        }
        val perfClass = when {
            totalRamMb >= 8192 && Build.VERSION.SDK_INT >= 34 -> DevicePerformanceClass.FLAGSHIP
            totalRamMb >= 6144 && Build.VERSION.SDK_INT >= 31 -> DevicePerformanceClass.HIGH
            totalRamMb >= 4096 -> DevicePerformanceClass.MODERATE
            else -> DevicePerformanceClass.BASELINE
        }
        val recommendedMode = when {
            aiCoreAvailable -> AiExecutionMode.SYSTEM_LOCAL
            ramTier >= RamTier.HIGH && availableStorageMb >= 2048 -> AiExecutionMode.CUSTOM_LOCAL
            ramTier >= RamTier.MEDIUM && availableStorageMb >= 500 -> AiExecutionMode.CUSTOM_LOCAL
            else -> AiExecutionMode.CLOUD
        }

        return DeviceCapabilityResult(
            androidVersion = Build.VERSION.RELEASE.split(".").firstOrNull()?.toIntOrNull() ?: 0,
            sdkInt = Build.VERSION.SDK_INT,
            aiCoreAvailable = aiCoreAvailable,
            aiCoreVersion = aiCoreVersion,
            mlKitGenAiAvailable = mlKitAvailable,
            ramTier = ramTier,
            totalRamMb = totalRamMb,
            availableStorageMb = availableStorageMb,
            performanceClass = perfClass,
            supportedAbis = abis,
            recommendedMode = recommendedMode
        )
    }

    private fun getTotalRamMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024)
    }

    private fun getAvailableStorageMb(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getPackageVersion(packageName: String): String? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
