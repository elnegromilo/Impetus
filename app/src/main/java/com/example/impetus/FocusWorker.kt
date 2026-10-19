package com.example.impetus

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Tarea en segundo plano que muestra el contenido del día. WorkManager la vuelve
 * a ejecutar aunque la app esté cerrada o el teléfono se haya reiniciado.
 */
class FocusWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            NotificationHelper.createNotificationChannel(applicationContext)
            NotificationHelper.showDailyNotification(
                applicationContext,
                DailyContentRepository.forToday(),
                ImpetusPreferences(applicationContext).notificationContent
            )
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "No se pudo mostrar la notificación diaria", exception)
            // failure() no reintenta; la próxima ejecución periódica sigue en pie.
            Result.failure()
        }
    }

    private companion object {
        const val TAG = "FocusWorker"
    }
}
