package com.example.euphony.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.euphony.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.euphony.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.euphony.NEXT"
        const val ACTION_PREVIOUS = "com.example.euphony.PREVIOUS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d("NotificationAction", "Received action: $action")

        when (action) {
            ACTION_PLAY_PAUSE -> {
                try {
                    val playerRepository = AppContainer.providePlayerRepository()
                    if (playerRepository.isPlaying.value) {
                        playerRepository.pause()
                        Log.d("NotificationAction", "Paused")
                    } else {
                        playerRepository.resume()
                        Log.d("NotificationAction", "Resumed")
                    }
                } catch (e: Exception) {
                    Log.e("NotificationAction", "Error toggling play/pause", e)
                }
            }
            ACTION_NEXT -> {
                scope.launch {
                    try {
                        AppContainer.provideFakeQueueRepository().playNext()
                        Log.d("NotificationAction", "Playing next")
                    } catch (e: Exception) {
                        Log.e("NotificationAction", "Error playing next", e)
                    }
                }
            }
            ACTION_PREVIOUS -> {
                scope.launch {
                    try {
                        AppContainer.provideFakeQueueRepository().playPrevious(forceSkip = true)
                        Log.d("NotificationAction", "Playing previous")
                    } catch (e: Exception) {
                        Log.e("NotificationAction", "Error playing previous", e)
                    }
                }
            }
            else -> {
                Log.w("NotificationAction", "Unknown action: $action")
            }
        }
    }
}