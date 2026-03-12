package com.healthjournal.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_ai_models")
data class LocalAiModelEntity(
    @PrimaryKey
    @ColumnInfo(name = "model_id") val modelId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "runtime_type") val runtimeType: String,
    @ColumnInfo(name = "file_format") val fileFormat: String,
    @ColumnInfo(name = "quantization") val quantization: String?,
    @ColumnInfo(name = "required_ram_mb") val requiredRamMb: Int,
    @ColumnInfo(name = "recommended_ram_mb") val recommendedRamMb: Int,
    @ColumnInfo(name = "size_mb") val sizeMb: Long,
    @ColumnInfo(name = "download_url") val downloadUrl: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "install_state") val installState: String,
    @ColumnInfo(name = "checksum") val checksum: String?,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "supports_structured_json") val supportsStructuredJson: Boolean,
    @ColumnInfo(name = "supports_streaming") val supportsStreaming: Boolean,
    @ColumnInfo(name = "supports_text_generation") val supportsTextGeneration: Boolean,
    @ColumnInfo(name = "is_active") val isActive: Boolean = false,
    @ColumnInfo(name = "installed_at") val installedAt: Long? = null
)
