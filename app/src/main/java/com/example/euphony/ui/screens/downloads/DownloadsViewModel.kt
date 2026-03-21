package com.example.euphony.ui.screens.downloads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphony.data.download.DownloadManager
import com.example.euphony.data.local.entity.DownloadEntity
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.QueueRepository
import com.example.euphony.domain.usecase.DeleteDownloadUseCase
import com.example.euphony.domain.usecase.GetDownloadsUseCase
import com.example.euphony.domain.usecase.PlaySongUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DownloadsUiState(
    val downloads: List<DownloadEntity> = emptyList(),
    val isLoading: Boolean = true,
    val totalSize: Long = 0,
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val downloadToDelete: DownloadEntity? = null
)

class DownloadsViewModel(
    private val getDownloadsUseCase: GetDownloadsUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val queueRepository: QueueRepository,
    private val downloadManager: DownloadManager
) : ViewModel() {

    companion object {
        private const val TAG = "DownloadsViewModel"
    }

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
        observeTotalSize()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            getDownloadsUseCase().collect { downloads ->
                Log.d(TAG, "Downloads updated: ${downloads.size}")
                _uiState.update { it.copy(downloads = downloads, isLoading = false) }
            }
        }
    }

    private fun observeTotalSize() {
        viewModelScope.launch {
            downloadManager.getTotalDownloadSize().collect { size ->
                _uiState.update { it.copy(totalSize = size ?: 0) }
            }
        }
    }

    fun playSong(download: DownloadEntity) {
        viewModelScope.launch {
            try {
                // Play this song in context of all downloads
                val allSongs = _uiState.value.downloads.map { it.toSong() }
                val startIndex = _uiState.value.downloads.indexOfFirst { it.videoId == download.videoId }

                if (allSongs.isNotEmpty() && startIndex >= 0) {
                    queueRepository.playQueue(allSongs, startIndex)
                    Log.d(TAG, "Playing offline song: ${download.title} (index $startIndex of ${allSongs.size})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing song", e)
                _uiState.update { it.copy(error = "Failed to play song") }
            }
        }
    }

    fun playAllDownloads(startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.downloads.map { it.toSong() }
                if (songs.isNotEmpty()) {
                    queueRepository.playQueue(songs, startIndex)
                    Log.d(TAG, "Playing all downloads from index $startIndex")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing downloads", e)
                _uiState.update { it.copy(error = "Failed to play downloads") }
            }
        }
    }

    fun shuffleDownloads() {
        viewModelScope.launch {
            try {
                val songs = _uiState.value.downloads.map { it.toSong() }.shuffled()
                if (songs.isNotEmpty()) {
                    queueRepository.playQueue(songs, 0)
                    Log.d(TAG, "Playing shuffled downloads")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error shuffling downloads", e)
                _uiState.update { it.copy(error = "Failed to shuffle downloads") }
            }
        }
    }

    fun showDeleteConfirmDialog(download: DownloadEntity) {
        _uiState.update {
            it.copy(showDeleteConfirmDialog = true, downloadToDelete = download)
        }
    }

    fun hideDeleteConfirmDialog() {
        _uiState.update {
            it.copy(showDeleteConfirmDialog = false, downloadToDelete = null)
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            try {
                deleteDownloadUseCase(videoId)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        downloadToDelete = null,
                        successMessage = "Download deleted"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting download", e)
                _uiState.update { it.copy(error = "Failed to delete download") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun DownloadEntity.toSong() = Song(
        id = videoId,
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        duration = duration
    )
}

