package com.example.euphony.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.euphony.MainActivity
import com.example.euphony.di.AppContainer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackService : MediaSessionService() {

    companion object {
        private const val TAG = "PlaybackService"
        private const val CHANNEL_ID = "euphony_media_channel_v2"
        private const val NOTIFICATION_ID = 1001
    }

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationManager: NotificationManager
    private var currentAlbumArt: Bitmap? = null

    override fun onCreate() {
        super.onCreate()
        try {
            notificationManager = getSystemService(NotificationManager::class.java)
            createNotificationChannel()

            startForeground(NOTIFICATION_ID, createBasicNotification())

            initializeSession()
            observePlaybackState()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Euphony Media Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media Playback Controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeSession() {
        try {
            val player = AppContainer.providePlayerRepository().getExoPlayer()
            if (player != null) {
                val forwardingPlayer = object : ForwardingPlayer(player) {
                    override fun getAvailableCommands(): Player.Commands {
                        return super.getAvailableCommands()
                            .buildUpon()
                            .addAll(
                                Player.COMMAND_PLAY_PAUSE,
                                Player.COMMAND_PREPARE,
                                Player.COMMAND_STOP,
                                Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
                                Player.COMMAND_SEEK_TO_NEXT,
                                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                                Player.COMMAND_SEEK_TO_PREVIOUS,
                                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                                Player.COMMAND_SEEK_BACK,
                                Player.COMMAND_SEEK_FORWARD
                            )
                            .build()
                    }

                    override fun hasNextMediaItem(): Boolean {
                        return AppContainer.provideFakeQueueRepository().canPlayNext()
                    }

                    override fun hasPreviousMediaItem(): Boolean {
                        return AppContainer.provideFakeQueueRepository().canPlayPrevious()
                    }

                    override fun seekToNext() {
                        Log.d(TAG, "ForwardingPlayer: seekToNext called")
                        // IMPORTANT: Register manual action immediately to block auto-play
                        AppContainer.provideFakeQueueRepository().prepareManualInteraction()

                        serviceScope.launch {
                            try {
                                AppContainer.provideFakeQueueRepository().playNext()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in playNext", e)
                            }
                        }
                    }

                    override fun seekToNextMediaItem() {
                        seekToNext()
                    }

                    override fun seekToPrevious() {
                        Log.d(TAG, "ForwardingPlayer: seekToPrevious called")
                        // IMPORTANT: Register manual action immediately
                        AppContainer.provideFakeQueueRepository().prepareManualInteraction()

                        serviceScope.launch {
                            try {
                                // Pass forceSkip=true to bypass "restart song" logic
                                AppContainer.provideFakeQueueRepository().playPrevious(forceSkip = true)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in seekToPrevious", e)
                            }
                        }
                    }

                    override fun seekToPreviousMediaItem() {
                        seekToPrevious()
                    }
                }

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                mediaSession = MediaSession.Builder(this, forwardingPlayer)
                    .setSessionActivity(pendingIntent)
                    .setCallback(MediaSessionCallback())
                    .build()

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updateNotification()
                        // Android 16: when playback ends demote from foreground
                        if ((playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE)
                            && !player.isPlaying) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    stopForeground(STOP_FOREGROUND_DETACH)
                                } else {
                                    @Suppress("DEPRECATION")
                                    stopForeground(false)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updateNotification()
                        if (isPlaying) {
                            // Re-promote to foreground when playback resumes
                            try {
                                startForeground(NOTIFICATION_ID, buildCurrentNotification())
                            } catch (_: Exception) {}
                        }
                    }
                    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                        updateNotification()
                    }
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing session", e)
        }
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(SessionCommand(SessionCommand.COMMAND_CODE_SESSION_SET_RATING))
                .build()

            val playerCommands = Player.Commands.Builder()
                .addAll(
                    Player.COMMAND_PLAY_PAUSE,
                    Player.COMMAND_PREPARE,
                    Player.COMMAND_STOP,
                    Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    Player.COMMAND_SEEK_BACK,
                    Player.COMMAND_SEEK_FORWARD
                )
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun observePlaybackState() {
        serviceScope.launch {
            AppContainer.provideQueueRepository().getQueueState().collectLatest { queueState ->
                if (queueState.currentSong != null) {
                    serviceScope.launch { loadAlbumArt(queueState.currentSong.thumbnailUrl) }
                    updateNotification()
                }
            }
        }
    }

    private suspend fun loadAlbumArt(thumbnailUrl: String) {
        try {
            val imageLoader = ImageLoader(this)
            val request = ImageRequest.Builder(this)
                .data(thumbnailUrl)
                .size(512, 512)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                currentAlbumArt = (result.drawable as? BitmapDrawable)?.bitmap
                updateNotification()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading art", e)
        }
    }

    private fun buildCurrentNotification(): Notification {
        return try {
            val queueState = AppContainer.provideFakeQueueRepository().getCurrentQueueStateSnapshot()
            val song = queueState.currentSong
            if (song != null) {
                buildRichNotification(song.title, song.artist, queueState.isPlaying, currentAlbumArt)
            } else {
                createBasicNotification()
            }
        } catch (e: Exception) {
            createBasicNotification()
        }
    }

    private fun updateNotification() {
        try {
            val queueRepository = AppContainer.provideFakeQueueRepository()
            val queueState = queueRepository.getCurrentQueueStateSnapshot()
            val song = queueState.currentSong

            val notification = if (song != null) {
                buildRichNotification(
                    title = song.title,
                    artist = song.artist,
                    isPlaying = queueState.isPlaying,
                    albumArt = currentAlbumArt
                )
            } else {
                createBasicNotification()
            }

            // Android 16: must call startForeground while actively playing
            if (queueState.isPlaying) {
                startForeground(NOTIFICATION_ID, notification)
            } else {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }

    private fun buildRichNotification(
        title: String,
        artist: String,
        isPlaying: Boolean,
        albumArt: Bitmap?
    ): Notification {

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = createActionIntent(NotificationActionReceiver.ACTION_PREVIOUS)
        val pauseIntent = createActionIntent(NotificationActionReceiver.ACTION_PLAY_PAUSE)
        val nextIntent = createActionIntent(NotificationActionReceiver.ACTION_NEXT)

        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous, "Previous", prevIntent
        )

        val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playLabel = if (isPlaying) "Pause" else "Play"
        val playAction = NotificationCompat.Action(
            playIcon, playLabel, pauseIntent
        )

        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next, "Next", nextIntent
        )

        val mediaStyle = MediaStyle().setShowActionsInCompactView(0, 1, 2)

        mediaSession?.let { session ->
            try {
                val token = MediaSessionCompat.Token.fromToken(session.platformToken)
                mediaStyle.setMediaSession(token)
            } catch (e: Exception) {
                Log.e(TAG, "Token conversion failed", e)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(albumArt)
            .setContentIntent(contentPendingIntent)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(prevAction)
            .addAction(playAction)
            .addAction(nextAction)
            .setStyle(mediaStyle)
            .build()
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(packageName) // Explicit package required on Android 16
            setClass(this@PlaybackService, NotificationActionReceiver::class.java)
        }
        return PendingIntent.getBroadcast(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createBasicNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Euphony")
            .setContentText("Ready to play")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}