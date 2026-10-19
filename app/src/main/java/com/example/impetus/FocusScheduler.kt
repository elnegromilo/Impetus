package com.example.impetus

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Programa la notificación diaria a la hora que eligió el usuario.
 */
object FocusScheduler {

    /** Nombre único de la tarea periódica en WorkManager. */
    const val WORK_NAME = "impetus_daily_focus_work"

    /** Hora por defecto del recordatorio. */
    const val DEFAULT_HOUR = 8

    /** Sube este número si cambia la forma de programar, para reprogramar instalaciones viejas. */
    private const val SCHEDULE_VERSION = 2

    /**
     * Se llama al abrir la app. Solo reprograma si la hora cambió (o es una instalación
     * nueva o anterior a esta versión); si no, mantiene la tarea existente.
     */
    fun ensureScheduled(context: Context) {
        val prefs = ImpetusPreferences(context)
        val time = prefs.reminderTime
        val signature = "v$SCHEDULE_VERSION-$time"
        val policy = if (prefs.scheduledSignature == signature) {
            ExistingPeriodicWorkPolicy.KEEP
        } else {
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
        }
        enqueue(context, time, policy)
        prefs.scheduledSignature = signature
    }

    /** Guarda una nueva hora y reprograma desde cero. */
    fun reschedule(context: Context, time: LocalTime) {
        ImpetusPreferences(context).reminderTime = time
        ensureScheduled(context)
    }

    private fun enqueue(context: Context, time: LocalTime, policy: ExistingPeriodicWorkPolicy) {
        val initialDelay = delayUntilNext(ZonedDateTime.now(ZoneId.systemDefault()), time)
        val request = PeriodicWorkRequestBuilder<FocusWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    /** Próximo momento en que llegará el recordatorio. */
    fun nextReminder(now: ZonedDateTime, time: LocalTime): ZonedDateTime = now.plus(delayUntilNext(now, time))

    /**
     * Tiempo desde [now] hasta la próxima vez que el reloj marque [time]. Si ya pasó
     * (o es justo esa hora), cuenta hasta mañana. Respeta el horario de verano.
     */
    internal fun delayUntilNext(now: ZonedDateTime, time: LocalTime): Duration {
        var next = now.with(time.withSecond(0).withNano(0))
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return Duration.between(now, next)
    }
}
