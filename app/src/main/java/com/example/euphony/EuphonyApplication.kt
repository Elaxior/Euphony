package com.example.euphony

import android.app.Application
import com.example.euphony.data.remote.DownloaderImpl
import com.example.euphony.di.AppContainer
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class EuphonyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeNewPipe()
        initializeAppContainer()
        // REMOVED: startPlaybackService() - This was causing the crash!
        // Service will start from MainActivity when user plays music
    }

    private fun initializeNewPipe() {
        try {
            // Initialize NewPipe with US localization for broader content access
            NewPipe.init(
                DownloaderImpl.getInstance(),
                Localization("en", "US"), // Use US English for better compatibility
                ContentCountry("US") // US content country for maximum availability
            )
            android.util.Log.d("EuphonyApplication", "NewPipe initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("EuphonyApplication", "Failed to initialize NewPipe", e)
            e.printStackTrace()
        }
    }

    private fun initializeAppContainer() {
        AppContainer.initialize(this)
    }
}
