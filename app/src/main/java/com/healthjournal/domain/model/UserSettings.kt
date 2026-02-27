package com.healthjournal.domain.model

import com.healthjournal.domain.model.ai.AiSettings

data class UserSettings(
    val userName: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val aiConsentGiven: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val languageMode: String = "SYSTEM",
    val aiSettings: AiSettings = AiSettings(),
    val activeProfileId: Long = 0
)
