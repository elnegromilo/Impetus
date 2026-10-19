package com.example.impetus

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Programa la notificación diaria a una hora fija del día.
 */
object FocusScheduler {

    /** Nombre único de la tarea periódica en WorkManager. */
    const val WORK_NAME = "impetus_daily_focus_work"

    /** Hora local (0-23) a la que debe llegar la notificación. */
    const val NOTIFICATION_HOUR = 8

    /**
     * Encola el trabajo diario con un retraso inicial hasta la próxima
     * [NOTIFICATION_HOUR]:00 local. WorkManager agrupa tareas, así que la hora
     * es aproximada (puede variar unos minutos).
     *
     * Usa UPDATE para que una instalación que ya tenía programada la tarea
     * (sin hora fija) adopte el nuevo horario sin duplicarse.
     */
    fun schedule(context: Context) {
        val initialDelay = delayUntilNext(ZonedDateTime.now(ZoneId.systemDefault()), NOTIFICATION_HOUR)

        val request = PeriodicWorkRequestBuilder<FocusWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Tiempo desde [now] hasta la próxima ocurrencia de [hour]:00. Si ya pasó
     * (o es justo esa hora), cuenta hasta mañana. Respeta cambios de horario de
     * verano porque trabaja con [ZonedDateTime].
     */
    internal fun delayUntilNext(now: ZonedDateTime, hour: Int): Duration {
        var next = now.withHour(hour).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return Duration.between(now, next)
    }
}
