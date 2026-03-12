package com.healthjournal.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class JsonFileStore<T>(
    context: Context,
    private val fileName: String,
    private val serializer: KSerializer<T>,
    private val json: Json,
    private val getId: (T) -> Long,
    private val setId: (T, Long) -> T
) {
    private val file = File(context.filesDir, fileName)
    private val mutex = Mutex()
    private val _data = MutableStateFlow<List<T>>(emptyList())
    private val nextId = AtomicLong(1)

    init {
        val loaded = loadFromFile()
        _data.value = loaded
        val maxId = loaded.maxOfOrNull { getId(it) } ?: 0
        nextId.set(maxId + 1)
    }

    fun observeAll(): Flow<List<T>> = _data

    fun observe(predicate: (T) -> Boolean): Flow<List<T>> =
        _data.map { list -> list.filter(predicate) }

    suspend fun getById(id: Long): T? = _data.value.find { getId(it) == id }

    suspend fun insert(item: T): Long = mutex.withLock {
        val id = nextId.getAndIncrement()
        val withId = setId(item, id)
        val updated = _data.value + withId
        _data.value = updated
        saveToFile(updated)
        id
    }

    suspend fun update(item: T) = mutex.withLock {
        val id = getId(item)
        val updated = _data.value.map { if (getId(it) == id) item else it }
        _data.value = updated
        saveToFile(updated)
    }

    suspend fun delete(item: T) = mutex.withLock {
        val id = getId(item)
        val updated = _data.value.filter { getId(it) != id }
        _data.value = updated
        saveToFile(updated)
    }

    suspend fun updateWhere(predicate: (T) -> Boolean, transform: (T) -> T) = mutex.withLock {
        val updated = _data.value.map { if (predicate(it)) transform(it) else it }
        _data.value = updated
        saveToFile(updated)
    }

    suspend fun deleteWhere(predicate: (T) -> Boolean) = mutex.withLock {
        val updated = _data.value.filterNot(predicate)
        _data.value = updated
        saveToFile(updated)
    }

    suspend fun replaceAll(items: List<T>) = mutex.withLock {
        _data.value = items
        val maxId = items.maxOfOrNull { getId(it) } ?: 0
        nextId.set(maxId + 1)
        saveToFile(items)
    }

    fun getAll(): List<T> = _data.value

    fun getFileName(): String = fileName

    private fun loadFromFile(): List<T> = try {
        if (file.exists()) {
            json.decodeFromString(ListSerializer(serializer), file.readText())
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun saveToFile(data: List<T>) {
        file.writeText(json.encodeToString(ListSerializer(serializer), data))
    }
}
