package com.example.impetus

import android.app.Application
import androidx.work.Configuration

/**
 * Clase Application personalizada de Impetus.
 * Permite inicializar WorkManager con configuración custom si es necesario.
 * Declarar en AndroidManifest: android:name=".ImpetusApp"
 */
class ImpetusApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

}