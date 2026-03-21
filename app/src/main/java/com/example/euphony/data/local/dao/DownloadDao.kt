package com.example.euphony.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.euphony.data.local.entity.DownloadEntity
import com.example.euphony.data.local.entity.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    /**
     * Add a download entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownload(download: DownloadEntity)

    /**
     * Update download status
     */
    @Update
    suspend fun updateDownload(download: DownloadEntity)

    /**
     * Delete a download
     */
    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    /**
     * Get all completed downloads (sorted by recently downloaded)
     */
    @Query("SELECT * FROM downloads WHERE downloadStatus = 'COMPLETED' ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    /**
     * Get download by videoId
     */
    @Query("SELECT * FROM downloads WHERE videoId = :videoId LIMIT 1")
    suspend fun getDownload(videoId: String): DownloadEntity?

    /**
     * Check if song is downloaded
     */
    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE videoId = :videoId AND downloadStatus = 'COMPLETED')")
    fun isDownloaded(videoId: String): Flow<Boolean>

    /**
     * Get download count
     */
    @Query("SELECT COUNT(*) FROM downloads WHERE downloadStatus = 'COMPLETED'")
    fun getDownloadCount(): Flow<Int>

    /**
     * Get total download size
     */
    @Query("SELECT SUM(fileSize) FROM downloads WHERE downloadStatus = 'COMPLETED'")
    fun getTotalDownloadSize(): Flow<Long?>

    /**
     * Clear all downloads
     */
    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()

    /**
     * Get downloads by status
     */
    @Query("SELECT * FROM downloads WHERE downloadStatus = :status ORDER BY downloadedAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>
}

