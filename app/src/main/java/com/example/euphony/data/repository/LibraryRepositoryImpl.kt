package com.example.euphony.data.repository

import com.example.euphony.data.local.dao.FavoriteDao
import com.example.euphony.data.local.dao.PlaylistDao
import com.example.euphony.data.local.entity.FavoriteEntity
import com.example.euphony.data.local.entity.PlaylistEntity
import com.example.euphony.data.local.entity.PlaylistSongCrossRefEntity
import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val favoriteDao: FavoriteDao,
    private val playlistDao: PlaylistDao
) : LibraryRepository {

    // ========== FAVORITES ==========

    override suspend fun addToFavorites(song: Song) {
        favoriteDao.addFavorite(song.toFavoriteEntity(isFavorite = true))
    }

    override suspend fun removeFromFavorites(videoId: String) {
        // Instead of deleting, just set isFavorite to false
        // This keeps the song if it's in playlists
        val existing = playlistDao.getSongEntity(videoId)
        if (existing != null) {
            favoriteDao.addFavorite(existing.copy(isFavorite = false))
        } else {
            favoriteDao.removeFavorite(videoId)
        }
    }

    override fun getFavorites(): Flow<List<Song>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.map { it.toSong() }
        }
    }

    override fun isFavorite(videoId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(videoId)
    }

    // ========== PLAYLISTS ==========

    override suspend fun createPlaylist(name: String): Long {
        val playlist = PlaylistEntity(name = name)
        return playlistDao.createPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        val playlistWithSongs = playlistDao.getPlaylistWithSongs(playlistId)
        playlistWithSongs?.let {
            val updated = it.playlist.copy(
                name = newName,
                updatedAt = System.currentTimeMillis()
            )
            playlistDao.updatePlaylist(updated)
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsWithSongs().map { playlistsWithSongs ->
            playlistsWithSongs.map { it.toDomainModel() }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistWithSongs(playlistId)?.toDomainModel()
    }

    // ========== PLAYLIST SONGS ==========

    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        // First, check if song already exists
        val existing = playlistDao.getSongEntity(song.videoId)

        if (existing != null) {
            // Song exists, keep its isFavorite status
            playlistDao.insertSongEntity(existing)
        } else {
            // New song, add with isFavorite=false (just for playlist)
            playlistDao.insertSongEntity(song.toFavoriteEntity(isFavorite = false))
        }

        // Then add to playlist
        val crossRef = PlaylistSongCrossRefEntity(
            playlistId = playlistId,
            videoId = song.videoId
        )
        playlistDao.addSongToPlaylist(crossRef)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, videoId)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongs(playlistId).map { songs ->
            songs.map { it.toSong() }
        }
    }

    // ========== MAPPERS ==========

    private fun Song.toFavoriteEntity(isFavorite: Boolean = false) = FavoriteEntity(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        isFavorite = isFavorite
    )

    private fun FavoriteEntity.toSong() = Song(
        id = videoId,
        title = title,
        artist = artist,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        videoId = videoId
    )

    private fun com.example.euphony.data.local.entity.PlaylistWithSongs.toDomainModel() = Playlist(
        id = playlist.id.toString(),
        name = playlist.name,
        songs = songs.map { it.toSong() },
        createdAt = playlist.createdAt
    )
}
