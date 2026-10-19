package com.example.impetus

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker responsable de mostrar la frase del día en segundo plano.
 * WorkManager lo vuelve a ejecutar aunque la app esté cerrada o el
 * dispositivo se haya reiniciado.
 */
class FocusWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val quote = QuoteRepository.getQuoteForToday()
            NotificationHelper.createNotificationChannel(applicationContext)
            NotificationHelper.showFocusNotification(applicationContext, quote)
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "No se pudo mostrar la notificación diaria", exception)
            // failure() no reintenta; la próxima ejecución periódica seguirá en pie.
            Result.failure()
        }
    }

    private companion object {
        const val TAG = "FocusWorker"
    }
}
