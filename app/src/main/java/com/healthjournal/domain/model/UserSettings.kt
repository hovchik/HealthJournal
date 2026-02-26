package com.healthjournal.domain.model

data class UserSettings(
    val userName: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val aiConsentGiven: Boolean = false,
    val onboardingCompleted: Boolean = false
)
