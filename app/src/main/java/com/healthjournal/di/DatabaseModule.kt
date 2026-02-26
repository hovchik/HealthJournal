package com.healthjournal.di

import android.content.Context
import androidx.room.Room
import com.healthjournal.data.local.HealthDatabase
import com.healthjournal.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, "health_journal.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSymptomDao(db: HealthDatabase): SymptomDao = db.symptomDao()

    @Provides
    fun provideVitalSignDao(db: HealthDatabase): VitalSignDao = db.vitalSignDao()

    @Provides
    fun provideMedicationDao(db: HealthDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideMedicationLogDao(db: HealthDatabase): MedicationLogDao = db.medicationLogDao()

    @Provides
    fun provideAiReportDao(db: HealthDatabase): AiReportDao = db.aiReportDao()
}
