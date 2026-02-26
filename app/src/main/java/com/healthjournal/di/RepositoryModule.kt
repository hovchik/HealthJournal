package com.healthjournal.di

import com.healthjournal.data.remote.ClaudeAiProvider
import com.healthjournal.data.repository.*
import com.healthjournal.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSymptomRepository(impl: SymptomRepositoryImpl): SymptomRepository

    @Binds
    @Singleton
    abstract fun bindVitalSignRepository(impl: VitalSignRepositoryImpl): VitalSignRepository

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindAiReportRepository(impl: AiReportRepositoryImpl): AiReportRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiProvider(impl: ClaudeAiProvider): AiProvider
}
