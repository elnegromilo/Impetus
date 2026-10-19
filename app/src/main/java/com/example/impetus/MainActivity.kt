package com.example.impetus

import android.Manifest
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity(), SettingsSheet.Host {

    private lateinit var prefs: ImpetusPreferences
    private lateinit var content: DailyContent
    private var currentTab = ContentTab.QUOTE

    private lateinit var dateView: TextView
    private lateinit var occasionView: TextView
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var contentBlock: View
    private lateinit var quoteMark: TextView
    private lateinit var contentView: TextView
    private lateinit var referenceView: TextView
    private lateinit var reminderCard: MaterialCardView
    private lateinit var reminderIcon: ImageView
    private lateinit var reminderText: TextView
    private lateinit var enableButton: MaterialButton

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        refreshReminderStatus()
        (supportFragmentManager.findFragmentByTag(SettingsSheet.TAG) as? SettingsSheet)?.refresh()
        if (isGranted) {
            showMessage(getString(R.string.reminder_enabled_confirmation, formatTime(prefs.reminderTime)))
        } else {
            showNotificationsDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = ImpetusPreferences(this)

        bindViews()
        applyWindowInsets()

        NotificationHelper.createNotificationChannel(this)
        FocusScheduler.ensureScheduled(this)

        currentTab = tabFromIntent(intent) ?: prefs.lastTab
        toggle.check(if (currentTab == ContentTab.QUOTE) R.id.btn_tab_quote else R.id.btn_tab_verse)
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val tab = if (checkedId == R.id.btn_tab_verse) ContentTab.VERSE else ContentTab.QUOTE
            if (tab != currentTab) {
                currentTab = tab
                prefs.lastTab = tab
                renderContent(animate = true)
            }
        }

        findViewById<View>(R.id.btn_settings).setOnClickListener { openSettings() }
        reminderCard.setOnClickListener { openSettings() }
        enableButton.setOnClickListener { requestNotifications() }
        findViewById<View>(R.id.btn_share).setOnClickListener { shareContent() }
        findViewById<View>(R.id.btn_copy).setOnClickListener { copyContent() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tabFromIntent(intent)?.let { tab ->
            toggle.check(if (tab == ContentTab.QUOTE) R.id.btn_tab_quote else R.id.btn_tab_verse)
        }
    }

    override fun onResume() {
        super.onResume()
        // Se recalcula aquí por si cambió el día o el usuario volvió de los ajustes del sistema.
        content = DailyContentRepository.forDate(LocalDate.now())
        renderHeader()
        renderContent(animate = false)
        refreshReminderStatus()
    }

    // ---------------------------------------------------------------------
    // Renderizado
    // ---------------------------------------------------------------------

    private fun bindViews() {
        dateView = findViewById(R.id.tv_date)
        occasionView = findViewById(R.id.tv_occasion)
        toggle = findViewById(R.id.toggle_content)
        contentBlock = findViewById(R.id.content_block)
        quoteMark = findViewById(R.id.tv_quote_mark)
        contentView = findViewById(R.id.tv_content)
        referenceView = findViewById(R.id.tv_reference)
        reminderCard = findViewById(R.id.card_reminder)
        reminderIcon = findViewById(R.id.iv_reminder)
        reminderText = findViewById(R.id.tv_reminder)
        enableButton = findViewById(R.id.btn_enable)
    }

    /** La app se dibuja detrás de las barras del sistema; aquí se deja el espacio justo. */
    private fun applyWindowInsets() {
        val root = findViewById<View>(R.id.content_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val side = resources.getDimensionPixelSize(R.dimen.screen_padding)
            view.updatePadding(
                left = side + bars.left,
                top = bars.top,
                right = side + bars.right,
                bottom = bars.bottom
            )
            insets
        }
    }

    private fun renderHeader() {
        val date = DATE_FORMAT.format(content.date)
        dateView.text = date.replaceFirstChar { it.titlecase(SPANISH) }

        val occasion = content.occasion
        occasionView.visibility = if (occasion != null) View.VISIBLE else View.GONE
        occasionView.text = occasion?.label
    }

    private fun renderContent(animate: Boolean) {
        val update = {
            val isQuote = currentTab == ContentTab.QUOTE
            val text = if (isQuote) content.quote else content.verse.text
            quoteMark.visibility = if (isQuote) View.VISIBLE else View.GONE
            contentView.text = text
            // Textos largos con letra algo más pequeña para que entren sin desplazar.
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (text.length > 160) 22f else 26f)
            referenceView.visibility = if (isQuote) View.GONE else View.VISIBLE
            referenceView.text = getString(R.string.verse_reference, content.verse.reference)
        }

        if (animate && ValueAnimator.areAnimatorsEnabled()) {
            contentBlock.animate().cancel()
            contentBlock.animate().alpha(0f).setDuration(120).withEndAction {
                update()
                contentBlock.animate().alpha(1f).setDuration(180).start()
            }.start()
        } else {
            contentBlock.alpha = 1f
            update()
        }
    }

    private fun refreshReminderStatus() {
        val enabled = NotificationHelper.areEnabled(this)
        enableButton.visibility = if (enabled) View.GONE else View.VISIBLE
        reminderIcon.setImageResource(if (enabled) R.drawable.ic_bell else R.drawable.ic_bell_off)
        reminderText.text = if (enabled) nextReminderText() else getString(R.string.reminder_disabled)
    }

    private fun nextReminderText(): String {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val time = prefs.reminderTime
        val next = FocusScheduler.nextReminder(now, time)
        val day = getString(if (next.toLocalDate() == now.toLocalDate()) R.string.reminder_today else R.string.reminder_tomorrow)
        val template = when (prefs.notificationContent) {
            NotificationContent.QUOTE -> R.string.reminder_next_quote
            NotificationContent.VERSE -> R.string.reminder_next_verse
            NotificationContent.BOTH -> R.string.reminder_next_both
        }
        return getString(template, day, formatTime(time))
    }

    // ---------------------------------------------------------------------
    // Acciones
    // ---------------------------------------------------------------------

    private fun currentShareText(): String = if (currentTab == ContentTab.QUOTE) {
        "“${content.quote}”\n\n— Impetus"
    } else {
        "“${content.verse.text}”\n${content.verse.reference} (RVR 1909)"
    }

    private fun shareContent() {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, currentShareText())
        startActivity(Intent.createChooser(send, null))
    }

    private fun copyContent() {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), currentShareText()))
        // Desde Android 13 el sistema ya muestra su propia confirmación; no la duplicamos.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showMessage(getString(R.string.copied))
        }
    }

    private fun openSettings() {
        if (supportFragmentManager.findFragmentByTag(SettingsSheet.TAG) == null) {
            SettingsSheet().show(supportFragmentManager, SettingsSheet.TAG)
        }
    }

    // ---------------------------------------------------------------------
    // SettingsSheet.Host
    // ---------------------------------------------------------------------

    override fun onReminderSettingsChanged() = refreshReminderStatus()

    override fun requestNotifications() {
        val canAskSystem = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED &&
            (!prefs.permissionRequested || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))

        if (canAskSystem) {
            prefs.permissionRequested = true
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // El sistema ya no muestra el diálogo (o se apagaron en ajustes): llevamos al usuario allí.
            openNotificationSettings()
        }
    }

    override fun sendTestNotification() {
        if (!NotificationHelper.areEnabled(this)) {
            requestNotifications()
            return
        }
        WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<FocusWorker>().build())
    }

    override fun formatTime(time: LocalTime): String {
        val pattern = if (DateFormat.is24HourFormat(this)) "H:mm" else "h:mm a"
        return DateTimeFormatter.ofPattern(pattern, SPANISH).format(time)
    }

    // ---------------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------------

    private fun openNotificationSettings() {
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        )
    }

    private fun showMessage(text: String) {
        Snackbar.make(findViewById(R.id.root), text, Snackbar.LENGTH_SHORT)
            .setAnchorView(reminderCard)
            .show()
    }

    private fun showNotificationsDeniedMessage() {
        Snackbar.make(findViewById(R.id.root), R.string.notifications_denied, Snackbar.LENGTH_LONG)
            .setAnchorView(reminderCard)
            .setAction(R.string.open_settings) { openNotificationSettings() }
            .show()
    }

    private fun tabFromIntent(intent: Intent?): ContentTab? =
        intent?.getStringExtra(EXTRA_TAB)?.let { name -> ContentTab.entries.firstOrNull { it.name == name } }

    companion object {
        const val EXTRA_TAB = "com.example.impetus.EXTRA_TAB"
        private val SPANISH: Locale = Locale.forLanguageTag("es-PE")
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", SPANISH)
    }
}
