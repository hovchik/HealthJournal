package com.hovchik.healthjournal.domain.model

import com.hovchik.healthjournal.domain.model.ai.AiSettings

data class UserSettings(
    val userName: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "",
    val knownDiseases: List<String> = emptyList(),
    val aiConsentGiven: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val termsAcceptedAt: Long = 0L,
    val languageMode: String = "SYSTEM",
    val themeMode: String = "SYSTEM",
    val aiSettings: AiSettings = AiSettings(),
    val activeProfileId: Long = 0
)
