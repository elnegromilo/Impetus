package com.example.impetus

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker responsable de ejecutar la tarea programada en segundo plano.
 * Garantiza la ejecución incluso si la app está cerrada o el dispositivo se reinicia.
 */
class FocusWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // 1. Obtener la frase del día desde nuestro repositorio local
            val quote = QuoteRepository.getRandomQuote()

            // 2. Asegurar que el canal de notificaciones exista en el sistema
            NotificationHelper.createNotificationChannel(applicationContext)

            // 3. Disparar la notificación push local con la frase
            NotificationHelper.showFocusNotification(applicationContext, quote)

            // 4. Reportar éxito total a WorkManager
            Result.success()

        } catch (exception: Exception) {
            exception.printStackTrace()
            // Reportar falla para no saturar el sistema de reintentos infinitos
            Result.failure()
        }
    }
}