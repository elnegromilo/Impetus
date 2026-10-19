package com.example.impetus

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalTime

/** Qué se envía en la notificación diaria. */
enum class NotificationContent { QUOTE, VERSE, BOTH }

/** Qué pestaña se ve en la pantalla principal. */
enum class ContentTab { QUOTE, VERSE }

/** Ajustes del usuario guardados en el dispositivo. */
class ImpetusPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("impetus_prefs", Context.MODE_PRIVATE)

    var reminderTime: LocalTime
        get() = LocalTime.of(
            prefs.getInt(KEY_HOUR, FocusScheduler.DEFAULT_HOUR),
            prefs.getInt(KEY_MINUTE, 0)
        )
        set(value) = prefs.edit().putInt(KEY_HOUR, value.hour).putInt(KEY_MINUTE, value.minute).apply()

    var notificationContent: NotificationContent
        get() = enumOrDefault(prefs.getString(KEY_CONTENT, null), NotificationContent.QUOTE)
        set(value) = prefs.edit().putString(KEY_CONTENT, value.name).apply()

    var lastTab: ContentTab
        get() = enumOrDefault(prefs.getString(KEY_TAB, null), ContentTab.QUOTE)
        set(value) = prefs.edit().putString(KEY_TAB, value.name).apply()

    /** Si ya se pidió el permiso de notificaciones al menos una vez. */
    var permissionRequested: Boolean
        get() = prefs.getBoolean(KEY_PERMISSION_ASKED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSION_ASKED, value).apply()

    /** Firma de la última programación, para no reprogramar si nada cambió. */
    var scheduledSignature: String?
        get() = prefs.getString(KEY_SIGNATURE, null)
        set(value) = prefs.edit().putString(KEY_SIGNATURE, value).apply()

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private companion object {
        const val KEY_HOUR = "reminder_hour"
        const val KEY_MINUTE = "reminder_minute"
        const val KEY_CONTENT = "notification_content"
        const val KEY_TAB = "last_tab"
        const val KEY_PERMISSION_ASKED = "permission_requested"
        const val KEY_SIGNATURE = "scheduled_signature"
    }
}
