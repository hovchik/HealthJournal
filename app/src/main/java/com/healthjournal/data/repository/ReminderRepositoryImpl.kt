package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.ReminderDto
import com.healthjournal.domain.model.Reminder
import com.healthjournal.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val store: JsonFileStore<ReminderDto>
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        store.observeAll().map { list -> list.map { it.toDomain() }.sortedBy { it.time } }

    override fun getEnabledReminders(): Flow<List<Reminder>> =
        store.observeAll().map { list -> list.filter { it.enabled }.map { it.toDomain() }.sortedBy { it.time } }

    override suspend fun getReminderById(id: Long): Reminder? =
        store.getById(id)?.toDomain()

    override suspend fun insertReminder(reminder: Reminder): Long =
        store.insert(ReminderDto.from(reminder))

    override suspend fun updateReminder(reminder: Reminder) =
        store.update(ReminderDto.from(reminder))

    override suspend fun deleteReminder(reminder: Reminder) =
        store.delete(ReminderDto.from(reminder))
}
