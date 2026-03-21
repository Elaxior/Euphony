package com.example.euphony.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.euphony.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    /**
     * Add song to favorites (replace if already exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    /**
     * Remove song from favorites
     */
    @Query("DELETE FROM favorites WHERE videoId = :videoId")
    suspend fun removeFavorite(videoId: String)

    /**
     * Get all favorites (sorted by recently added)
     * Returns Flow for reactive UI updates
     * Only returns songs where isFavorite = true
     */
    @Query("SELECT * FROM favorites WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    /**
     * Check if a song is favorited
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE videoId = :videoId AND isFavorite = 1)")
    fun isFavorite(videoId: String): Flow<Boolean>

    /**
     * Get favorite count
     */
    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>

    /**
     * Clear all favorites
     */
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}
