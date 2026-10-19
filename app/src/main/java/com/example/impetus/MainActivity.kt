package com.example.impetus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    /**
     * registerForActivityResult es la forma moderna y segura respecto al ciclo
     * de vida de pedir permisos en tiempo de ejecución (Android 13+ / API 33+).
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            FocusScheduler.schedule(this)
            showMessage(R.string.notifications_enabled)
        } else {
            showNotificationsDisabledMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // El canal debe existir antes de mostrar cualquier notificación.
        NotificationHelper.createNotificationChannel(this)

        setupTestButton()
        checkNotificationPermissionAndSchedule()
    }

    override fun onResume() {
        super.onResume()
        // Se actualiza en onResume por si cambió el día mientras la app estaba en segundo plano.
        findViewById<TextView>(R.id.tv_daily_quote).text = QuoteRepository.getQuoteForToday()
    }

    private fun setupTestButton() {
        findViewById<Button>(R.id.btn_test).setOnClickListener {
            if (hasNotificationPermission()) {
                WorkManager.getInstance(this)
                    .enqueue(OneTimeWorkRequestBuilder<FocusWorker>().build())
            } else {
                showNotificationsDisabledMessage()
            }
        }
    }

    /**
     * En Android 13+ POST_NOTIFICATIONS es un permiso de tiempo de ejecución.
     * En versiones anteriores se concede con solo declararlo en el Manifest.
     */
    private fun checkNotificationPermissionAndSchedule() {
        when {
            hasNotificationPermission() -> FocusScheduler.schedule(this)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> FocusScheduler.schedule(this)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showMessage(messageRes: Int) {
        Snackbar.make(findViewById<View>(android.R.id.content), messageRes, Snackbar.LENGTH_SHORT)
            .show()
    }

    /** Avisa que las notificaciones están apagadas y ofrece abrir los ajustes de la app. */
    private fun showNotificationsDisabledMessage() {
        Snackbar.make(
            findViewById<View>(android.R.id.content),
            R.string.notifications_disabled,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.open_settings) {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            )
        }.show()
    }
}
