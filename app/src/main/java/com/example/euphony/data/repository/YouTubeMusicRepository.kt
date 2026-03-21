package com.example.euphony.data.repository

import com.example.euphony.data.remote.YouTubeDataSource
import com.example.euphony.data.remote.mapper.SongMapper.toSongs
import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class YouTubeMusicRepository(
    private val youtubeDataSource: YouTubeDataSource
) : MusicRepository {

    override suspend fun getRecommendedSongs(): Result<List<Song>> {
        return youtubeDataSource.getTrendingSongs()
            .map { it.toSongs() }
    }

    // DEPRECATED: Use LibraryRepository instead
    override fun getLibraryPlaylists(): Flow<List<Playlist>> {
        return flowOf(emptyList()) // No longer used
    }

    override suspend fun getRecentlyPlayed(): Result<List<Song>> {
        return Result.success(emptyList())
    }

    override suspend fun searchSongs(query: String): Result<List<Song>> {
        return youtubeDataSource.searchSongs(query)
            .map { it.toSongs() }
    }

    // DEPRECATED: Use LibraryRepository instead
    override suspend fun getPlaylistById(playlistId: String): Result<Playlist> {
        return Result.failure(Exception("Use LibraryRepository for playlists"))
    }
}
