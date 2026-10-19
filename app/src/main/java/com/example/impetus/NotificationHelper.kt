package com.example.impetus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_ID = "impetus_daily_focus"
    private const val NOTIFICATION_ID = 1001
    private const val TAG = "NotificationHelper"

    /** Crea el canal de notificaciones (requerido desde Android 8.0). Es idempotente. */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** Si el sistema permite que la app muestre notificaciones (permiso y ajustes). */
    fun areEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Muestra el contenido del día según lo que el usuario eligió recibir. */
    fun showDailyNotification(context: Context, content: DailyContent, mode: NotificationContent) {
        if (!areEnabled(context)) return

        val verseLine = context.getString(R.string.verse_with_reference, content.verse.text, content.verse.reference)
        val (title, body) = when (mode) {
            NotificationContent.QUOTE -> context.getString(R.string.notification_title_quote) to content.quote
            NotificationContent.VERSE -> context.getString(R.string.notification_title_verse) to verseLine
            NotificationContent.BOTH -> context.getString(R.string.notification_title_both) to
                "${content.quote}\n\n$verseLine"
        }
        val fullTitle = content.occasion?.let { "${it.label} · $title" } ?: title

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val tab = if (mode == NotificationContent.VERSE) ContentTab.VERSE else ContentTab.QUOTE
            putExtra(MainActivity.EXTRA_TAB, tab.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(context.getColor(R.color.impetus_accent))
            .setContentTitle(fullTitle)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notificación omitida: falta el permiso POST_NOTIFICATIONS", e)
        }
    }
}
