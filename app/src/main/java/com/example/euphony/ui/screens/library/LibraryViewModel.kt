package com.example.euphony.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.QueueRepository
import com.example.euphony.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val favorites: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylist: Playlist? = null,
    val showPlaylistDetail: Boolean = false,
    val isLoading: Boolean = true,
    val showCreatePlaylistDialog: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val showRenamePlaylistDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showSongOptionsDialog: Boolean = false,
    val selectedSongForPlaylist: Song? = null,
    val selectedSongForOptions: Song? = null,
    val playlistToDelete: Playlist? = null,
    val playlistToRename: Playlist? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class LibraryViewModel(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val queueRepository: QueueRepository,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val renamePlaylistUseCase: RenamePlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LibraryViewModel"
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibraryData()
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            // Observe favorites
            launch {
                getFavoritesUseCase().collect { favorites ->
                    Log.d(TAG, "Favorites updated: ${favorites.size}")
                    _uiState.update { it.copy(favorites = favorites, isLoading = false) }
                }
            }

            // Observe playlists
            launch {
                getAllPlaylistsUseCase().collect { playlists ->
                    Log.d(TAG, "Playlists updated: ${playlists.size}")
                    _uiState.update { it.copy(playlists = playlists, isLoading = false) }
                }
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(song)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
                _uiState.update { it.copy(error = "Failed to update favorite") }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                createPlaylistUseCase(name)
                _uiState.update { it.copy(showCreatePlaylistDialog = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating playlist", e)
                _uiState.update { it.copy(error = e.message ?: "Failed to create playlist") }
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            try {
                addSongToPlaylistUseCase(playlistId, song)
                _uiState.update { it.copy(showAddToPlaylistDialog = false, selectedSongForPlaylist = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding song to playlist", e)
                _uiState.update { it.copy(error = "Failed to add song to playlist") }
            }
        }
    }

    fun showCreatePlaylistDialog() {
        _uiState.update { it.copy(showCreatePlaylistDialog = true) }
    }

    fun hideCreatePlaylistDialog() {
        _uiState.update { it.copy(showCreatePlaylistDialog = false) }
    }

    fun showAddToPlaylistDialog(song: Song) {
        _uiState.update { it.copy(showAddToPlaylistDialog = true, selectedSongForPlaylist = song) }
    }

    fun hideAddToPlaylistDialog() {
        _uiState.update { it.copy(showAddToPlaylistDialog = false, selectedSongForPlaylist = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    // ========== PLAY FUNCTIONS ==========

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song)
                Log.d(TAG, "Playing song: ${song.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing song", e)
                _uiState.update { it.copy(error = "Failed to play song") }
            }
        }
    }

    fun playFavorites(startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                val favorites = _uiState.value.favorites
                if (favorites.isNotEmpty()) {
                    queueRepository.playQueue(favorites, startIndex)
                    Log.d(TAG, "Playing favorites queue from index $startIndex")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing favorites", e)
                _uiState.update { it.copy(error = "Failed to play favorites") }
            }
        }
    }

    fun shuffleFavorites() {
        viewModelScope.launch {
            try {
                val favorites = _uiState.value.favorites
                if (favorites.isNotEmpty()) {
                    queueRepository.playQueue(favorites.shuffled(), 0)
                    Log.d(TAG, "Playing shuffled favorites")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error shuffling favorites", e)
                _uiState.update { it.copy(error = "Failed to shuffle favorites") }
            }
        }
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0, shuffle: Boolean = false) {
        viewModelScope.launch {
            try {
                if (playlist.songs.isNotEmpty()) {
                    val songs = if (shuffle) playlist.songs.shuffled() else playlist.songs
                    queueRepository.playQueue(songs, if (shuffle) 0 else startIndex)
                    Log.d(TAG, "Playing playlist: ${playlist.name}, shuffle: $shuffle")
                    _uiState.update { it.copy(successMessage = "Playing ${playlist.name}") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing playlist", e)
                _uiState.update { it.copy(error = "Failed to play playlist") }
            }
        }
    }

    // ========== PLAYLIST MANAGEMENT ==========

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlistId)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        playlistToDelete = null,
                        successMessage = "Playlist deleted"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting playlist", e)
                _uiState.update { it.copy(error = "Failed to delete playlist") }
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            try {
                renamePlaylistUseCase(playlistId, newName)
                _uiState.update {
                    it.copy(
                        showRenamePlaylistDialog = false,
                        playlistToRename = null,
                        successMessage = "Playlist renamed"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming playlist", e)
                _uiState.update { it.copy(error = "Failed to rename playlist") }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        viewModelScope.launch {
            try {
                removeSongFromPlaylistUseCase(playlistId, videoId)
                _uiState.update { it.copy(successMessage = "Song removed from playlist") }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing song from playlist", e)
                _uiState.update { it.copy(error = "Failed to remove song") }
            }
        }
    }

    // ========== DIALOG STATE MANAGEMENT ==========

    fun showPlaylistDetail(playlist: Playlist) {
        _uiState.update { it.copy(selectedPlaylist = playlist, showPlaylistDetail = true) }
    }

    fun hidePlaylistDetail() {
        _uiState.update { it.copy(selectedPlaylist = null, showPlaylistDetail = false) }
    }

    fun showDeleteConfirmDialog(playlist: Playlist) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, playlistToDelete = playlist) }
    }

    fun hideDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, playlistToDelete = null) }
    }

    fun showRenameDialog(playlist: Playlist) {
        _uiState.update { it.copy(showRenamePlaylistDialog = true, playlistToRename = playlist) }
    }

    fun hideRenameDialog() {
        _uiState.update { it.copy(showRenamePlaylistDialog = false, playlistToRename = null) }
    }

    fun showSongOptionsDialog(song: Song) {
        _uiState.update { it.copy(showSongOptionsDialog = true, selectedSongForOptions = song) }
    }

    fun hideSongOptionsDialog() {
        _uiState.update { it.copy(showSongOptionsDialog = false, selectedSongForOptions = null) }
    }
}
