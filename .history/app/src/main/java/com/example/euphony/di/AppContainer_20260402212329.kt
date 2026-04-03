package com.example.euphony.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.euphony.data.download.DownloadManager
import com.example.euphony.data.local.MusicDatabase
import com.example.euphony.data.remote.AudioStreamExtractor
import com.example.euphony.data.remote.LyricsFetcher
import com.example.euphony.data.remote.RelatedSongsFetcher
import com.example.euphony.data.remote.TrendingMusicFetcher
import com.example.euphony.data.remote.YouTubeDataSource
import com.example.euphony.data.repository.ExoPlayerRepository
import com.example.euphony.data.repository.FakeQueueRepository
import com.example.euphony.data.repository.LibraryRepositoryImpl
import com.example.euphony.data.repository.YouTubeMusicRepository
import com.example.euphony.domain.repository.LibraryRepository
import com.example.euphony.domain.repository.MusicRepository
import com.example.euphony.domain.repository.PlayerRepository
import com.example.euphony.domain.repository.QueueRepository
import com.example.euphony.domain.usecase.*
import com.example.euphony.ui.screens.home.HomeViewModel
import com.example.euphony.ui.screens.search.SearchViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppContainer {

    private lateinit var applicationContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Keep track of the pending auto-play job so we can cancel it
    private var autoPlayJob: Job? = null

    // ==================== DATABASE ====================

    private val database: MusicDatabase by lazy {
        MusicDatabase.getInstance(applicationContext)
    }

    private val historyDao by lazy {
        database.historyDao()
    }

    private val searchHistoryDao by lazy {
        database.searchHistoryDao()
    }

    // NEW: Library DAOs
    private val favoriteDao by lazy {
        database.favoriteDao()
    }

    private val playlistDao by lazy {
        database.playlistDao()
    }

    private val downloadDao by lazy {
        database.downloadDao()
    }

    // ==================== DATA SOURCES ====================

    private val youtubeDataSource: YouTubeDataSource by lazy {
        YouTubeDataSource()
    }

    private val audioStreamExtractor: AudioStreamExtractor by lazy {
        AudioStreamExtractor()
    }

    private val relatedSongsFetcher: RelatedSongsFetcher by lazy {
        RelatedSongsFetcher()
    }

    private val trendingMusicFetcher: TrendingMusicFetcher by lazy {
        TrendingMusicFetcher()
    }

    private val lyricsFetcher: LyricsFetcher by lazy {
        LyricsFetcher()
    }

    // ==================== REPOSITORIES ====================

    private val musicRepository: MusicRepository by lazy {
        YouTubeMusicRepository(youtubeDataSource)
    }

    // NEW: Download Manager - Create before playerRepository
    private val downloadManager: DownloadManager by lazy {
        DownloadManager(
            context = applicationContext,
            audioStreamExtractor = audioStreamExtractor,
            downloadDao = downloadDao
        )
    }

    private val playerRepository: ExoPlayerRepository by lazy {
        ExoPlayerRepository(
            context = applicationContext,
            audioStreamExtractor = audioStreamExtractor,
            downloadManager = downloadManager
        )
    }

    private val queueRepository: FakeQueueRepository by lazy {
        FakeQueueRepository(playerRepository, relatedSongsFetcher)
    }

    // NEW: Library Repository
    private val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            favoriteDao = favoriteDao,
            playlistDao = playlistDao
        )
    }

    // ==================== INITIALIZATION ====================

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        setupAutoPlayCallback()
    }

    private fun setupAutoPlayCallback() {
        playerRepository.setOnSongCompletedCallback {
            // Cancel any previous pending job
            autoPlayJob?.cancel()

            // Start a new job with a small delay (Debounce)
            autoPlayJob = scope.launch {
                val repeatMode = queueRepository.getCurrentQueueStateSnapshot().playbackMode.repeatMode

                // REPEAT_ONE: use minimal delay to avoid gap, and it bypasses manual guard internally
                val debounceMs = if (repeatMode == com.example.euphony.domain.model.RepeatMode.REPEAT_ONE) {
                    100L
                } else {
                    500L // Standard debounce for normal auto-play
                }

                delay(debounceMs)

                // If we are still here (job wasn't cancelled by manual action), trigger next.
                queueRepository.playNext(isAutoPlay = true)
            }
        }
    }

    fun cancelPendingAutoPlay() {
        if (autoPlayJob?.isActive == true) {
            autoPlayJob?.cancel()
        }
    }

    // ==================== USE CASES ====================

    // ========== Home Use Cases ==========

    fun provideGetHomeRecommendationsUseCase(): GetHomeRecommendationsUseCase {
        return GetHomeRecommendationsUseCase(historyDao, trendingMusicFetcher)
    }

    fun provideGetLibraryPlaylistsUseCase(): GetLibraryPlaylistsUseCase {
        return GetLibraryPlaylistsUseCase(libraryRepository)
    } // ✅ CORRECT

    // ========== Queue Use Cases ==========

    fun provideGetQueueStateUseCase(): GetQueueStateUseCase {
        return GetQueueStateUseCase(queueRepository)
    }

    // ========== Search Use Cases ==========

    fun provideSearchSongsUseCase(): SearchSongsUseCase {
        return SearchSongsUseCase(musicRepository)
    }

    fun provideAddSearchToHistoryUseCase(): AddSearchToHistoryUseCase {
        return AddSearchToHistoryUseCase(searchHistoryDao)
    }

    fun provideGetRecentSearchesUseCase(): GetRecentSearchesUseCase {
        return GetRecentSearchesUseCase(searchHistoryDao)
    }

    fun provideClearSearchHistoryUseCase(): ClearSearchHistoryUseCase {
        return ClearSearchHistoryUseCase(searchHistoryDao)
    }

    fun provideDeleteSearchHistoryItemUseCase(): DeleteSearchHistoryItemUseCase {
        return DeleteSearchHistoryItemUseCase(searchHistoryDao)
    }

    // ========== Playback Use Cases ==========

    fun providePlaySongUseCase(): PlaySongUseCase {
        return PlaySongUseCase(queueRepository, provideAddToHistoryUseCase())
    }

    fun provideAddToHistoryUseCase(): AddToHistoryUseCase {
        return AddToHistoryUseCase(historyDao)
    }

    fun providePauseSongUseCase(): PauseSongUseCase {
        return PauseSongUseCase(playerRepository)
    }

    fun provideResumeSongUseCase(): ResumeSongUseCase {
        return ResumeSongUseCase(playerRepository)
    }

    fun providePlayNextSongUseCase(): PlayNextSongUseCase {
        return PlayNextSongUseCase(queueRepository)
    }

    fun providePlayPreviousSongUseCase(): PlayPreviousSongUseCase {
        return PlayPreviousSongUseCase(queueRepository)
    }

    fun provideSeekToPositionUseCase(): SeekToPositionUseCase {
        return SeekToPositionUseCase(playerRepository)
    }

    fun provideToggleShuffleUseCase(): ToggleShuffleUseCase {
        return ToggleShuffleUseCase(queueRepository)
    }

    fun provideCycleRepeatModeUseCase(): CycleRepeatModeUseCase {
        return CycleRepeatModeUseCase(queueRepository)
    }

    // ========== NEW: Library Use Cases ==========

    fun provideGetFavoritesUseCase(): GetFavoritesUseCase {
        return GetFavoritesUseCase(libraryRepository)
    }

    fun provideToggleFavoriteUseCase(): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(libraryRepository)
    }

    fun provideGetAllPlaylistsUseCase(): GetAllPlaylistsUseCase {
        return GetAllPlaylistsUseCase(libraryRepository)
    }

    fun provideCreatePlaylistUseCase(): CreatePlaylistUseCase {
        return CreatePlaylistUseCase(libraryRepository)
    }

    fun provideAddSongToPlaylistUseCase(): AddSongToPlaylistUseCase {
        return AddSongToPlaylistUseCase(libraryRepository)
    }

    fun provideDeletePlaylistUseCase(): DeletePlaylistUseCase {
        return DeletePlaylistUseCase(libraryRepository)
    }

    fun provideRemoveSongFromPlaylistUseCase(): RemoveSongFromPlaylistUseCase {
        return RemoveSongFromPlaylistUseCase(libraryRepository)
    }

    fun provideRenamePlaylistUseCase(): RenamePlaylistUseCase {
        return RenamePlaylistUseCase(libraryRepository)
    }

    // ========== NEW: Download Use Cases ==========

    fun provideDownloadSongUseCase(): DownloadSongUseCase {
        return DownloadSongUseCase(downloadManager)
    }

    fun provideDeleteDownloadUseCase(): DeleteDownloadUseCase {
        return DeleteDownloadUseCase(downloadManager)
    }

    fun provideGetDownloadsUseCase(): GetDownloadsUseCase {
        return GetDownloadsUseCase(downloadManager)
    }

    fun provideDownloadManager(): DownloadManager {
        return downloadManager
    }

    fun provideLyricsFetcher(): LyricsFetcher {
        return lyricsFetcher
    }

    // ==================== REPOSITORY PROVIDERS ====================

    // Direct access for service and global state
    fun providePlayerRepository(): PlayerRepository {
        return playerRepository
    }

    fun provideQueueRepository(): QueueRepository {
        return queueRepository
    }

    fun provideFakeQueueRepository(): FakeQueueRepository {
        return queueRepository
    }

    // NEW: Direct access to LibraryRepository if needed
    fun provideLibraryRepository(): LibraryRepository {
        return libraryRepository
    }

    // ==================== VIEWMODEL FACTORIES ====================

    fun provideHomeViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(
                        getHomeRecommendationsUseCase = provideGetHomeRecommendationsUseCase(),
                        playSongUseCase = providePlaySongUseCase()
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    fun provideSearchViewModelFactory(): SearchViewModelFactory {
        return SearchViewModelFactory(
            provideSearchSongsUseCase(),
            provideAddSearchToHistoryUseCase(),
            provideGetRecentSearchesUseCase(),
            provideClearSearchHistoryUseCase(),
            provideDeleteSearchHistoryItemUseCase()
        )
    }

    // NEW: Library ViewModel Factory (optional - can also use direct providers in screen)
    fun provideLibraryViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(com.example.euphony.ui.screens.library.LibraryViewModel::class.java)) {
                    return com.example.euphony.ui.screens.library.LibraryViewModel(
                        getFavoritesUseCase = provideGetFavoritesUseCase(),
                        getAllPlaylistsUseCase = provideGetAllPlaylistsUseCase(),
                        toggleFavoriteUseCase = provideToggleFavoriteUseCase(),
                        createPlaylistUseCase = provideCreatePlaylistUseCase(),
                        addSongToPlaylistUseCase = provideAddSongToPlaylistUseCase(),
                        playSongUseCase = providePlaySongUseCase(),
                        queueRepository = queueRepository,
                        deletePlaylistUseCase = provideDeletePlaylistUseCase(),
                        renamePlaylistUseCase = provideRenamePlaylistUseCase(),
                        removeSongFromPlaylistUseCase = provideRemoveSongFromPlaylistUseCase()
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
