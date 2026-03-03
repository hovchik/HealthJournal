package com.healthjournal.domain.model

data class FamilyMember(
    val id: Long = 0,
    val name: String,
    val relationship: String = "",
    val avatarColor: Int = 0xFF1B6B4D.toInt(),
    val weight: String = "",
    val height: String = "",
    val knownDiseases: List<String> = emptyList()
)
