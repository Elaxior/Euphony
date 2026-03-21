package com.example.euphony.domain.repository

import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    // Favorites
    suspend fun addToFavorites(song: Song)
    suspend fun removeFromFavorites(videoId: String)
    fun getFavorites(): Flow<List<Song>>
    fun isFavorite(videoId: String): Flow<Boolean>

    // Playlists
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    // Playlist Songs
    suspend fun addSongToPlaylist(playlistId: Long, song: Song)
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>
}
