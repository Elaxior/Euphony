package com.example.euphony.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.euphony.data.local.dao.DownloadDao
import com.example.euphony.data.local.dao.FavoriteDao
import com.example.euphony.data.local.dao.HistoryDao
import com.example.euphony.data.local.dao.PlaylistDao
import com.example.euphony.data.local.dao.SearchHistoryDao
import com.example.euphony.data.local.entity.DownloadEntity
import com.example.euphony.data.local.entity.FavoriteEntity
import com.example.euphony.data.local.entity.HistoryEntity
import com.example.euphony.data.local.entity.PlaylistEntity
import com.example.euphony.data.local.entity.PlaylistSongCrossRefEntity
import com.example.euphony.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        HistoryEntity::class,
        SearchHistoryEntity::class,
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRefEntity::class,
        DownloadEntity::class          // NEW - Downloads
    ],
    version = 5,  // INCREASED VERSION for downloads
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao      // NEW

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "euphony_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
