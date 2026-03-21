package com.example.euphony.data.download

import android.content.Context
import android.util.Log
import com.example.euphony.data.local.dao.DownloadDao
import com.example.euphony.data.local.entity.DownloadEntity
import com.example.euphony.data.local.entity.DownloadStatus
import com.example.euphony.data.remote.AudioStreamExtractor
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Manages song downloads
 */
class DownloadManager(
    private val context: Context,
    private val audioStreamExtractor: AudioStreamExtractor,
    private val downloadDao: DownloadDao
) {
    companion object {
        private const val TAG = "DownloadManager"
        private const val DOWNLOADS_DIR = "downloads"
        private const val BUFFER_SIZE = 128 * 1024 // 128KB buffer for fast downloads
        private const val PROGRESS_UPDATE_THRESHOLD = 256 * 1024 // Update UI every 256KB (faster feedback)
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    /**
     * Get downloads directory
     */
    private fun getDownloadsDirectory(): File {
        val dir = File(context.filesDir, DOWNLOADS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Download a song
     */
    suspend fun downloadSong(song: Song): Result<DownloadEntity> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting download: ${song.title}")

            // Check if already downloaded
            val existing = downloadDao.getDownload(song.videoId)
            if (existing != null && existing.downloadStatus == DownloadStatus.COMPLETED) {
                Log.d(TAG, "Song already downloaded")
                return@withContext Result.success(existing)
            }

            // Create pending download entry
            val downloadEntity = DownloadEntity(
                videoId = song.videoId,
                title = song.title,
                artist = song.artist,
                thumbnailUrl = song.thumbnailUrl,
                duration = song.duration,
                filePath = "",
                fileSize = 0,
                downloadStatus = DownloadStatus.DOWNLOADING
            )
            downloadDao.addDownload(downloadEntity)

            // Extract audio stream URL
            val streamResult = audioStreamExtractor.getAudioStreamUrl(song.videoId)
            if (streamResult.isFailure) {
                val error = streamResult.exceptionOrNull()?.message ?: "Failed to get stream URL"
                Log.e(TAG, "Stream extraction failed: $error")
                downloadDao.updateDownload(downloadEntity.copy(downloadStatus = DownloadStatus.FAILED))
                return@withContext Result.failure(Exception(error))
            }

            val streamUrl = streamResult.getOrNull()!!

            // Download audio file
            val request = Request.Builder()
                .url(streamUrl)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                downloadDao.updateDownload(downloadEntity.copy(downloadStatus = DownloadStatus.FAILED))
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            // Save to file
            val fileName = "${song.videoId}.m4a"
            val outputFile = File(getDownloadsDirectory(), fileName)
            val inputStream = response.body?.byteStream()
            val outputStream = FileOutputStream(outputFile)

            val totalBytes = response.body?.contentLength() ?: 0
            var downloadedBytes = 0L
            var lastProgressUpdate = 0L
            val startTime = System.currentTimeMillis()

            // Initialize progress to 0% immediately
            _downloadProgress.value = _downloadProgress.value + (song.videoId to 0)
            Log.d(TAG, "Starting download: ${song.title}, Size: ${totalBytes / 1024}KB")

            inputStream?.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(BUFFER_SIZE) // 128KB buffer for fast downloads
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        // Update progress every 256KB or when complete for responsive feedback
                        if (totalBytes > 0 &&
                            (downloadedBytes - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD ||
                             downloadedBytes >= totalBytes)) {

                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            _downloadProgress.value = _downloadProgress.value + (song.videoId to progress)
                            lastProgressUpdate = downloadedBytes
                            Log.d(TAG, "Download progress: ${song.title} - $progress%")
                        }
                    }
                }
            }

            val downloadTime = System.currentTimeMillis() - startTime
            val speedMBps = if (downloadTime > 0) {
                (totalBytes / 1024.0 / 1024.0) / (downloadTime / 1000.0)
            } else {
                0.0
            }

            Log.d(TAG, "✅ Download completed: ${song.title} (${totalBytes / 1024 / 1024}MB in ${downloadTime}ms, ${String.format("%.2f", speedMBps)}MB/s)")

            // Update download entry with completed status
            val completedDownload = downloadEntity.copy(
                filePath = outputFile.absolutePath,
                fileSize = outputFile.length(),
                downloadStatus = DownloadStatus.COMPLETED,
                downloadedAt = System.currentTimeMillis()
            )
            downloadDao.updateDownload(completedDownload)

            // Clear progress
            _downloadProgress.value = _downloadProgress.value - song.videoId

            Result.success(completedDownload)

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)

            // Update status to failed
            try {
                val failed = downloadDao.getDownload(song.videoId)
                failed?.let {
                    downloadDao.updateDownload(it.copy(downloadStatus = DownloadStatus.FAILED))
                }
            } catch (dbError: Exception) {
                Log.e(TAG, "Failed to update download status", dbError)
            }

            _downloadProgress.value = _downloadProgress.value - song.videoId
            Result.failure(e)
        }
    }

    /**
     * Delete a download
     */
    suspend fun deleteDownload(videoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val download = downloadDao.getDownload(videoId)
            if (download != null) {
                // Delete file
                val file = File(download.filePath)
                if (file.exists()) {
                    file.delete()
                }

                // Delete from database
                downloadDao.deleteDownload(videoId)
                Log.d(TAG, "Download deleted: $videoId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete download", e)
            Result.failure(e)
        }
    }

    /**
     * Get all downloads
     */
    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads()
    }

    /**
     * Check if song is downloaded
     */
    fun isDownloaded(videoId: String): Flow<Boolean> {
        return downloadDao.isDownloaded(videoId)
    }

    /**
     * Get download
     */
    suspend fun getDownload(videoId: String): DownloadEntity? {
        return downloadDao.getDownload(videoId)
    }

    /**
     * Get total download size
     */
    fun getTotalDownloadSize(): Flow<Long?> {
        return downloadDao.getTotalDownloadSize()
    }

    /**
     * Clear all downloads
     */
    suspend fun clearAllDownloads(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete all files
            val downloadsDir = getDownloadsDirectory()
            downloadsDir.listFiles()?.forEach { it.delete() }

            // Clear database
            downloadDao.clearAllDownloads()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear downloads", e)
            Result.failure(e)
        }
    }
}

