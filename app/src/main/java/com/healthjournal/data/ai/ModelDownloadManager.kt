package com.healthjournal.data.ai

import com.healthjournal.domain.model.ai.InstallProgress
import com.healthjournal.domain.model.ai.ModelInstallState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Download manager for LLM model files. Uses HTTP range requests to download
 * multiple chunks in parallel and merge them, significantly improving throughput
 * on high-bandwidth connections. Supports pause/resume and automatic retry.
 */
class ModelDownloadManager {

    companion object {
        /** Number of concurrent download chunks */
        private const val CHUNK_COUNT = 4

        /** Minimum file size to use chunked download (5 MB) */
        private const val MIN_SIZE_FOR_CHUNKS = 5L * 1024 * 1024

        /** Buffer size for streaming (64 KB for better throughput) */
        private const val BUFFER_SIZE = 64 * 1024

        /** Max retries per chunk on transient failure */
        private const val MAX_CHUNK_RETRIES = 3

        /** Progress emission throttle interval (ms) */
        private const val PROGRESS_INTERVAL_MS = 250L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "HealthJournal-Android/1.0")
                    .build()
            )
        }
        .build()

    private val _progress = MutableStateFlow<InstallProgress?>(null)
    val progress: StateFlow<InstallProgress?> = _progress.asStateFlow()

    private var activeJob: Job? = null
    private var pauseRequested = false
    private val pauseMutex = Mutex()

    /**
     * Download a file from [url] into [targetFile]. Uses parallel chunked
     * downloads when the server supports range requests and the file is large
     * enough. Falls back to a single-stream download otherwise.
     *
     * The download can be resumed if a previous attempt left partial chunk files.
     */
    suspend fun download(
        modelId: String,
        url: String,
        targetFile: File,
        scope: CoroutineScope
    ): Result<File> {
        pauseRequested = false
        val tmpFile = File(targetFile.parent, "${targetFile.name}.tmp")
        targetFile.parentFile?.mkdirs()

        return withContext(Dispatchers.IO) {
            try {
                // HEAD request to get content-length and check range support
                val headRequest = Request.Builder().url(url).head().build()
                val headResponse = client.newCall(headRequest).execute()

                val totalBytes = headResponse.header("Content-Length")?.toLongOrNull() ?: -1L
                val acceptsRanges = headResponse.header("Accept-Ranges")
                    ?.equals("bytes", ignoreCase = true) == true
                headResponse.close()

                if (totalBytes > MIN_SIZE_FOR_CHUNKS && acceptsRanges) {
                    chunkedDownload(modelId, url, tmpFile, targetFile, totalBytes, scope)
                } else {
                    singleStreamDownload(modelId, url, tmpFile, targetFile, totalBytes)
                }
            } catch (e: CancellationException) {
                _progress.value = InstallProgress(
                    modelId, ModelInstallState.DOWNLOADING, isPaused = true,
                    bytesDownloaded = _progress.value?.bytesDownloaded ?: 0,
                    totalBytes = _progress.value?.totalBytes ?: 0
                )
                Result.failure(e)
            } catch (e: Exception) {
                _progress.value = InstallProgress(
                    modelId, ModelInstallState.FAILED,
                    errorMessage = e.message ?: "Download failed"
                )
                Result.failure(e)
            }
        }
    }

    fun pause() {
        pauseRequested = true
        activeJob?.cancel()
    }

    fun isPaused(): Boolean = pauseRequested

    // ---- Chunked parallel download ----

    private suspend fun chunkedDownload(
        modelId: String,
        url: String,
        tmpFile: File,
        targetFile: File,
        totalBytes: Long,
        scope: CoroutineScope
    ): Result<File> {
        val chunkSize = totalBytes / CHUNK_COUNT
        val chunkFiles = (0 until CHUNK_COUNT).map { index ->
            File(tmpFile.parent, "${tmpFile.name}.part$index")
        }

        val globalBytesDownloaded = AtomicLong(0L)
        var lastEmitTime = 0L
        val speedTracker = SpeedTracker()

        // Check for previously downloaded chunk bytes (resume support)
        chunkFiles.forEachIndexed { index, file ->
            if (file.exists()) {
                val chunkStart = index * chunkSize
                val chunkEnd = if (index == CHUNK_COUNT - 1) totalBytes - 1 else (index + 1) * chunkSize - 1
                val expected = chunkEnd - chunkStart + 1
                if (file.length() in 1 until expected) {
                    globalBytesDownloaded.addAndGet(file.length())
                } else if (file.length() == expected) {
                    globalBytesDownloaded.addAndGet(expected)
                } else {
                    file.delete()
                }
            }
        }

        emitProgress(modelId, globalBytesDownloaded.get(), totalBytes, speedTracker)

        // Launch parallel chunk downloads
        val job = scope.launch(Dispatchers.IO) {
            val chunkJobs = (0 until CHUNK_COUNT).map { index ->
                val chunkStart = index * chunkSize
                val chunkEnd = if (index == CHUNK_COUNT - 1) totalBytes - 1
                else (index + 1) * chunkSize - 1
                val chunkFile = chunkFiles[index]

                async {
                    downloadChunk(
                        url = url,
                        chunkFile = chunkFile,
                        rangeStart = chunkStart,
                        rangeEnd = chunkEnd,
                        onBytesRead = { delta ->
                            val total = globalBytesDownloaded.addAndGet(delta)
                            speedTracker.recordBytes(delta)
                            val now = System.currentTimeMillis()
                            if (now - lastEmitTime > PROGRESS_INTERVAL_MS) {
                                lastEmitTime = now
                                emitProgress(modelId, total, totalBytes, speedTracker)
                            }
                        }
                    )
                }
            }
            chunkJobs.awaitAll()
        }
        activeJob = job

        try {
            job.join()
        } catch (_: CancellationException) {
            if (pauseRequested) {
                _progress.value = InstallProgress(
                    modelId, ModelInstallState.DOWNLOADING, isPaused = true,
                    bytesDownloaded = globalBytesDownloaded.get(),
                    totalBytes = totalBytes,
                    progressPercent = ((globalBytesDownloaded.get() * 100) / totalBytes).toInt()
                )
                return Result.failure(PauseException())
            }
            throw CancellationException()
        }

        // Merge chunks into target
        emitProgress(modelId, totalBytes, totalBytes, speedTracker)
        mergeChunks(chunkFiles, tmpFile)
        tmpFile.renameTo(targetFile)

        // Cleanup chunk files
        chunkFiles.forEach { it.delete() }

        _progress.value = InstallProgress(
            modelId, ModelInstallState.INSTALLING, 100,
            bytesDownloaded = totalBytes, totalBytes = totalBytes
        )
        return Result.success(targetFile)
    }

    private suspend fun downloadChunk(
        url: String,
        chunkFile: File,
        rangeStart: Long,
        rangeEnd: Long,
        onBytesRead: (Long) -> Unit
    ) {
        val expectedSize = rangeEnd - rangeStart + 1
        var existingBytes = if (chunkFile.exists()) chunkFile.length() else 0L

        // Already complete
        if (existingBytes == expectedSize) return

        // If the partial file is corrupt (larger than expected), restart
        if (existingBytes > expectedSize) {
            chunkFile.delete()
            existingBytes = 0L
        }

        val actualStart = rangeStart + existingBytes

        retryWithBackoff(MAX_CHUNK_RETRIES) {
            checkPause()

            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=$actualStart-$rangeEnd")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful && response.code != 206) {
                response.close()
                throw DownloadException("HTTP ${response.code} for range $actualStart-$rangeEnd")
            }

            val body = response.body ?: throw DownloadException("Empty body for chunk")

            RandomAccessFile(chunkFile, "rw").use { raf ->
                raf.seek(existingBytes)
                body.byteStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        checkPause()
                        raf.write(buffer, 0, read)
                        onBytesRead(read.toLong())
                    }
                }
            }
            response.close()
        }
    }

    // ---- Single-stream fallback ----

    private suspend fun singleStreamDownload(
        modelId: String,
        url: String,
        tmpFile: File,
        targetFile: File,
        totalBytes: Long
    ): Result<File> {
        var existingBytes = if (tmpFile.exists()) tmpFile.length() else 0L
        val speedTracker = SpeedTracker()
        var lastEmitTime = 0L

        retryWithBackoff(MAX_CHUNK_RETRIES) {
            checkPause()

            val requestBuilder = Request.Builder().url(url)
            if (existingBytes > 0) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
            }
            val response = client.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful && response.code != 206) {
                response.close()
                throw DownloadException("HTTP ${response.code}")
            }

            val body = response.body ?: throw DownloadException("Empty response body")

            val effectiveTotal = if (response.code == 206) {
                // Parse Content-Range: bytes start-end/total
                response.header("Content-Range")
                    ?.substringAfterLast("/")
                    ?.toLongOrNull() ?: totalBytes
            } else {
                body.contentLength().let { if (it > 0) it else totalBytes }
            }

            RandomAccessFile(tmpFile, "rw").use { raf ->
                raf.seek(existingBytes)
                body.byteStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        checkPause()
                        raf.write(buffer, 0, read)
                        existingBytes += read
                        speedTracker.recordBytes(read.toLong())
                        val now = System.currentTimeMillis()
                        if (now - lastEmitTime > PROGRESS_INTERVAL_MS) {
                            lastEmitTime = now
                            emitProgress(modelId, existingBytes, effectiveTotal, speedTracker)
                        }
                    }
                }
            }
            response.close()
        }

        tmpFile.renameTo(targetFile)
        _progress.value = InstallProgress(
            modelId, ModelInstallState.INSTALLING, 100,
            bytesDownloaded = targetFile.length(), totalBytes = targetFile.length()
        )
        return Result.success(targetFile)
    }

    // ---- Helpers ----

    private fun mergeChunks(chunkFiles: List<File>, output: File) {
        output.outputStream().use { out ->
            chunkFiles.forEach { chunk ->
                chunk.inputStream().use { it.copyTo(out, BUFFER_SIZE) }
            }
        }
    }

    private fun emitProgress(
        modelId: String,
        bytesDownloaded: Long,
        totalBytes: Long,
        speedTracker: SpeedTracker
    ) {
        val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0
        _progress.value = InstallProgress(
            modelId = modelId,
            state = ModelInstallState.DOWNLOADING,
            progressPercent = percent,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            bytesPerSecond = speedTracker.getBytesPerSecond()
        )
    }

    private suspend fun checkPause() {
        if (pauseRequested) throw CancellationException("Download paused")
    }

    private suspend fun <T> retryWithBackoff(maxRetries: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay((1000L * (1 shl attempt)).coerceAtMost(8000L))
                }
            }
        }
        throw lastException ?: DownloadException("Failed after $maxRetries retries")
    }

    /**
     * Tracks download speed using a sliding window of 3 seconds.
     */
    private class SpeedTracker {
        private data class Sample(val timestamp: Long, val bytes: Long)

        private val samples = mutableListOf<Sample>()
        private val lock = Any()
        private val windowMs = 3000L

        fun recordBytes(bytes: Long) {
            synchronized(lock) {
                samples.add(Sample(System.currentTimeMillis(), bytes))
                pruneOld()
            }
        }

        fun getBytesPerSecond(): Long {
            synchronized(lock) {
                pruneOld()
                if (samples.isEmpty()) return 0
                val oldest = samples.first().timestamp
                val elapsed = System.currentTimeMillis() - oldest
                if (elapsed < 100) return 0
                val totalBytes = samples.sumOf { it.bytes }
                return (totalBytes * 1000L) / elapsed
            }
        }

        private fun pruneOld() {
            val cutoff = System.currentTimeMillis() - windowMs
            samples.removeAll { it.timestamp < cutoff }
        }
    }

    class DownloadException(message: String) : Exception(message)
    class PauseException : Exception("Download paused by user")
}
