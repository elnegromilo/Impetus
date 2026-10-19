package com.example.impetus

import android.app.Application
import android.util.Log
import androidx.work.Configuration

/**
 * Application de Impetus. Aquí se configura WorkManager.
 */
class ImpetusApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
