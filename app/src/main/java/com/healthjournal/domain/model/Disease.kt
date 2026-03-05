package com.healthjournal.domain.model

import java.time.LocalDateTime

data class Disease(
    val id: Long = 0,
    val name: String,
    val profileId: Long = 0,
    val notes: String = "",
    val active: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
