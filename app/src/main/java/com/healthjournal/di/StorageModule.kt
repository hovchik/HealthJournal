package com.healthjournal.di

import android.content.Context
import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

    @Provides
    @Singleton
    fun provideStorageJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    @Provides
    @Singleton
    fun provideSymptomStore(
        @ApplicationContext context: Context,
        json: Json
    ): JsonFileStore<SymptomDto> = JsonFileStore(
        context = context,
        fileName = "symptoms.json",
        serializer = SymptomDto.serializer(),
        json = json,
        getId = { it.id },
        setId = { item, id -> item.copy(id = id) }
    )

    @Provides
    @Singleton
    fun provideVitalSignStore(
        @ApplicationContext context: Context,
        json: Json
    ): JsonFileStore<VitalSignDto> = JsonFileStore(
        context = context,
        fileName = "vital_signs.json",
        serializer = VitalSignDto.serializer(),
        json = json,
        getId = { it.id },
        setId = { item, id -> item.copy(id = id) }
    )

    @Provides
    @Singleton
    fun provideMedicationStore(
        @ApplicationContext context: Context,
        json: Json
    ): JsonFileStore<MedicationDto> = JsonFileStore(
        context = context,
        fileName = "medications.json",
        serializer = MedicationDto.serializer(),
        json = json,
        getId = { it.id },
        setId = { item, id -> item.copy(id = id) }
    )

    @Provides
    @Singleton
    fun provideMedicationLogStore(
        @ApplicationContext context: Context,
        json: Json
    ): JsonFileStore<MedicationLogDto> = JsonFileStore(
        context = context,
        fileName = "medication_logs.json",
        serializer = MedicationLogDto.serializer(),
        json = json,
        getId = { it.id },
        setId = { item, id -> item.copy(id = id) }
    )

    @Provides
    @Singleton
    fun provideAiReportStore(
        @ApplicationContext context: Context,
        json: Json
    ): JsonFileStore<AiReportDto> = JsonFileStore(
        context = context,
        fileName = "ai_reports.json",
        serializer = AiReportDto.serializer(),
        json = json,
        getId = { it.id },
        setId = { item, id -> item.copy(id = id) }
    )
}
