package com.example.euphony.data.repository

import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMusicRepository : MusicRepository {

    private val fakeSongs = listOf(
        Song(
            id = "1",
            title = "Neon Dreams",
            artist = "Synthwave Collective",
            album = "Electric Nights",
            duration = 245000,
            thumbnailUrl = "",
            videoId = "demo1"
        ),
        Song(
            id = "2",
            title = "Urban Pulse",
            artist = "Beat Masters",
            album = "City Sounds",
            duration = 198000,
            thumbnailUrl = "",
            videoId = "demo2"
        ),
        Song(
            id = "3",
            title = "Midnight Echo",
            artist = "Ambient Waves",
            album = "Night Sessions",
            duration = 312000,
            thumbnailUrl = "",
            videoId = "demo3"
        ),
        Song(
            id = "4",
            title = "Digital Sunrise",
            artist = "Electro Fusion",
            album = "New Dawn",
            duration = 267000,
            thumbnailUrl = "",
            videoId = "demo4"
        ),
        Song(
            id = "5",
            title = "Cosmic Journey",
            artist = "Space Harmony",
            album = "Beyond Stars",
            duration = 289000,
            thumbnailUrl = "",
            videoId = "demo5"
        )
    )

    private val fakePlaylists = listOf(
        Playlist(
            id = "p1",
            name = "Favorites",
            songs = fakeSongs.take(3)
        ),
        Playlist(
            id = "p2",
            name = "Workout Mix",
            songs = fakeSongs.takeLast(2)
        )
    )

    override suspend fun getRecommendedSongs(): Result<List<Song>> {
        // Simulate network delay
        delay(500)
        return Result.success(fakeSongs)
    }

    override fun getLibraryPlaylists(): Flow<List<Playlist>> {
        return flowOf(fakePlaylists)
    }

    override suspend fun getRecentlyPlayed(): Result<List<Song>> {
        delay(300)
        return Result.success(fakeSongs.take(3))
    }

    override suspend fun searchSongs(query: String): Result<List<Song>> {
        delay(400)
        val filtered = fakeSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }
        return Result.success(filtered)
    }

    override suspend fun getPlaylistById(playlistId: String): Result<Playlist> {
        delay(200)
        val playlist = fakePlaylists.find { it.id == playlistId }
        return if (playlist != null) {
            Result.success(playlist)
        } else {
            Result.failure(Exception("Playlist not found"))
        }
    }
}