package com.example.euphony.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.euphony.data.local.entity.FavoriteEntity
import com.example.euphony.data.local.entity.PlaylistEntity
import com.example.euphony.data.local.entity.PlaylistSongCrossRefEntity
import com.example.euphony.data.local.entity.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // ========== PLAYLIST CRUD ==========

    /**
     * Create a new playlist
     * Returns the generated playlist ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    /**
     * Update playlist metadata
     */
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    /**
     * Delete a playlist (cascade deletes playlist_songs entries)
     */
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    /**
     * Get all playlists (without songs)
     */
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    /**
     * Get playlist with all its songs
     * Uses @Transaction for consistency
     */
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithSongs(playlistId: Long): PlaylistWithSongs?

    /**
     * Get all playlists with their songs
     */
    @Transaction
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    // ========== PLAYLIST SONGS CRUD ==========

    /**
     * Add song to playlist
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRefEntity)

    /**
     * Add song entity (if not exists) - for foreign key integrity
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongEntity(song: FavoriteEntity)

    /**
     * Remove song from playlist
     */
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)

    /**
     * Get songs in a playlist (sorted by position)
     */
    @Query("""
        SELECT f.* FROM favorites f
        INNER JOIN playlist_songs ps ON f.videoId = ps.videoId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
    """)
    fun getPlaylistSongs(playlistId: Long): Flow<List<FavoriteEntity>>

    /**
     * Check if song exists in playlist
     */
    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND videoId = :videoId)")
    suspend fun isSongInPlaylist(playlistId: Long, videoId: String): Boolean

    /**
     * Get playlist count
     */
    @Query("SELECT COUNT(*) FROM playlists")
    fun getPlaylistCount(): Flow<Int>

    /**
     * Update playlist timestamp when songs are modified
     */
    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Get song entity by videoId (to check if exists)
     */
    @Query("SELECT * FROM favorites WHERE videoId = :videoId LIMIT 1")
    suspend fun getSongEntity(videoId: String): FavoriteEntity?
}
