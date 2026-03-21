package com.example.euphony.domain.repository

import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    /**
     * Get recommended songs for home screen
     */
    suspend fun getRecommendedSongs(): Result<List<Song>>

    /**
     * Get all playlists in library
     */
    fun getLibraryPlaylists(): Flow<List<Playlist>>

    /**
     * Get recently played songs
     */
    suspend fun getRecentlyPlayed(): Result<List<Song>>

    /**
     * Search for songs
     */
    suspend fun searchSongs(query: String): Result<List<Song>>

    /**
     * Get playlist by id
     */
    suspend fun getPlaylistById(playlistId: String): Result<Playlist>
}
