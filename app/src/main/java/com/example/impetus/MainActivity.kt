package com.example.impetus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // -------------------------------------------------------------------------
    // CONSTANTES
    // -------------------------------------------------------------------------
    companion object {
        /**
         * Tag único que identifica nuestra tarea periódica en WorkManager.
         * Usar KEEP en ExistingPeriodicWorkPolicy evita duplicados si el usuario
         * abre la app múltiples veces.
         */
        const val WORK_NAME = "impetus_daily_focus_work"
    }

    // -------------------------------------------------------------------------
    // LAUNCHER DE PERMISOS (Android 13+ / API 33+)
    // -------------------------------------------------------------------------
    /**
     * registerForActivityResult es la forma moderna y lifecycle-safe
     * de manejar permisos en tiempo de ejecución.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido -> programar el worker
            scheduleDailyFocusWork()
            Toast.makeText(this, "✅ Notificaciones activadas", Toast.LENGTH_SHORT).show()
        } else {
            // Permiso denegado -> informar al usuario sin bloquear la app
            Toast.makeText(
                this,
                "Las notificaciones están desactivadas. Puedes cambiarlas en Ajustes.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // -------------------------------------------------------------------------
    // LIFECYCLE
    // -------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // <- Corregido aquí
        setContentView(R.layout.activity_main)

        // 1. Crear el canal de notificaciones (siempre, antes de todo)
        NotificationHelper.createNotificationChannel(this)

        // 2. Configurar el botón de prueba
        setupTestButton()

        // 3. Solicitar permiso y programar el trabajo periódico
        checkNotificationPermissionAndSchedule()
    }

    // -------------------------------------------------------------------------
    // UI SETUP
    // -------------------------------------------------------------------------
    private fun setupTestButton() {
        findViewById<Button>(R.id.btn_test).setOnClickListener {
            // Verifica permiso antes de intentar mostrar la notificación
            if (hasNotificationPermission()) {

                // === CAMBIO AQUÍ ===
                val testWorkRequest = androidx.work.OneTimeWorkRequestBuilder<FocusWorker>().build()
                androidx.work.WorkManager.getInstance(this).enqueue(testWorkRequest)
                // ===================

            } else {
                Toast.makeText(this, "Activa las notificaciones primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // -------------------------------------------------------------------------
    // PERMISOS
    // -------------------------------------------------------------------------
    /**
     * En Android 13+ (API 33+) POST_NOTIFICATIONS es un permiso de tiempo
     * de ejecución. En versiones anteriores, se concede automáticamente
     * con la declaración en el Manifest.
     */
    private fun checkNotificationPermissionAndSchedule() {
        when {
            // Caso 1: Ya tiene el permiso -> programar directamente
            hasNotificationPermission() -> {
                scheduleDailyFocusWork()
            }
            // Caso 2: Android 13+ -> solicitar permiso en tiempo de ejecución
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            // Caso 3: Android < 13 -> permiso implícito, programar directamente
            else -> {
                scheduleDailyFocusWork()
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Versiones anteriores a Android 13 no requieren permiso explícito
        }
    }

    // -------------------------------------------------------------------------
    // WORKMANAGER
    // -------------------------------------------------------------------------
    /**
     * Programa el FocusWorker para ejecutarse cada 24 horas.
     *
     * ExistingPeriodicWorkPolicy.KEEP es crítico:
     * - Si ya existe una tarea con el mismo WORK_NAME, la mantiene intacta.
     * - Evita duplicar la tarea cada vez que el usuario abre la app.
     */
    private fun scheduleDailyFocusWork() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<FocusWorker>(
            24,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}