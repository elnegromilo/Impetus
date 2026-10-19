package com.example.impetus

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime

/**
 * Ajustes del recordatorio en una hoja inferior: hora, qué contenido llega y una prueba.
 * Los cambios se guardan al instante (no hay botón "Guardar" que olvidar).
 */
class SettingsSheet : BottomSheetDialogFragment() {

    /** Lo que la hoja necesita de la pantalla que la abre. */
    interface Host {
        fun onReminderSettingsChanged()
        fun requestNotifications()
        fun sendTestNotification()
        fun formatTime(time: LocalTime): String
    }

    private val host: Host get() = requireActivity() as Host
    private lateinit var prefs: ImpetusPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.sheet_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = ImpetusPreferences(requireContext())

        view.findViewById<View>(R.id.row_time).setOnClickListener { showTimePicker() }
        view.findViewById<View>(R.id.btn_sheet_enable).setOnClickListener { host.requestNotifications() }
        view.findViewById<View>(R.id.btn_test).setOnClickListener {
            host.sendTestNotification()
            if (NotificationHelper.areEnabled(requireContext())) {
                Toast.makeText(requireContext(), R.string.settings_test_sent, Toast.LENGTH_SHORT).show()
            }
        }

        val group = view.findViewById<RadioGroup>(R.id.rg_content)
        group.check(
            when (prefs.notificationContent) {
                NotificationContent.QUOTE -> R.id.rb_quote
                NotificationContent.VERSE -> R.id.rb_verse
                NotificationContent.BOTH -> R.id.rb_both
            }
        )
        group.setOnCheckedChangeListener { _, checkedId ->
            prefs.notificationContent = when (checkedId) {
                R.id.rb_verse -> NotificationContent.VERSE
                R.id.rb_both -> NotificationContent.BOTH
                else -> NotificationContent.QUOTE
            }
            host.onReminderSettingsChanged()
        }

        val version = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName ?: ""
        view.findViewById<TextView>(R.id.tv_footer).text = getString(R.string.settings_footer, version)

        // Si el selector de hora seguía abierto (por ejemplo, tras girar el teléfono), se reconecta.
        (childFragmentManager.findFragmentByTag(TIME_PICKER_TAG) as? MaterialTimePicker)?.let(::attach)
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Actualiza lo que depende del sistema (permiso) o de otros cambios. */
    fun refresh() {
        val view = view ?: return
        view.findViewById<TextView>(R.id.tv_time_value).text = host.formatTime(prefs.reminderTime)
        view.findViewById<View>(R.id.card_notifications_off).visibility =
            if (NotificationHelper.areEnabled(requireContext())) View.GONE else View.VISIBLE
    }

    private fun showTimePicker() {
        if (childFragmentManager.findFragmentByTag(TIME_PICKER_TAG) != null) return
        val current = prefs.reminderTime
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(current.hour)
            .setMinute(current.minute)
            .setTitleText(R.string.settings_time_picker_title)
            .build()
        attach(picker)
        picker.show(childFragmentManager, TIME_PICKER_TAG)
    }

    private fun attach(picker: MaterialTimePicker) {
        picker.addOnPositiveButtonClickListener {
            FocusScheduler.reschedule(requireContext(), LocalTime.of(picker.hour, picker.minute))
            refresh()
            host.onReminderSettingsChanged()
        }
    }

    companion object {
        const val TAG = "settings_sheet"
        private const val TIME_PICKER_TAG = "time_picker"
    }
}
