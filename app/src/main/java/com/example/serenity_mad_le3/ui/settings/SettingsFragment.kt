package com.example.serenity_mad_le3.ui.settings

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Settings
import com.example.serenity_mad_le3.workers.HydrationWorker
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {
    private companion object {
        private const val TAG = "SettingsFragment"
    }

    private lateinit var prefs: Prefs
    private var pendingSettings: Settings? = null

    private var notificationsSwitch: SwitchMaterial? = null
    private var intervalSlider: Slider? = null
    private var intervalLabel: TextView? = null
    private var darkModeSwitch: SwitchMaterial? = null

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pending = pendingSettings ?: return@registerForActivityResult
        pendingSettings = null
        if (!isAdded) return@registerForActivityResult

        if (granted) {
            Log.i(TAG, "POST_NOTIFICATIONS granted; enabling reminders.")
            persistSettings(pending)
        } else {
            val fallback = pending.copy(notificationsEnabled = false)
            notificationsSwitch?.isChecked = false
            persistSettings(fallback, showToast = false)
            context?.let { ctx ->
                val message = ctx.getString(R.string.hydration_reminder_permission_denied)
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val content = inflater.inflate(R.layout.fragment_settings, container, false)
        return wrapIfLandscape(content)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())
        val settings = prefs.getSettings()

        // Ensure WorkManager reflects the latest saved preference before any changes.
        scheduleOrCancel(settings)

        // Gather UI references once; these components drive the hydration reminder controls.
        notificationsSwitch = view.findViewById(R.id.switchNotifications)
        intervalSlider = view.findViewById(R.id.sliderInterval)
        intervalLabel = view.findViewById(R.id.textInterval)
        val save = view.findViewById<MaterialButton>(R.id.btnSaveSettings)
        val darkIcon = view.findViewById<ImageView>(R.id.iconDarkMode)
        darkModeSwitch = view.findViewById(R.id.switchDarkMode)

        // Decorative icon keeps the dark mode toggle from feeling bare.
        darkIcon.setImageDrawable(
            IconicsDrawable(requireContext(), FontAwesome.Icon.faw_moon).apply {
                sizeDp = 20
                colorInt = ContextCompat.getColor(requireContext(), R.color.secondary_30)
            }
        )

        notificationsSwitch?.isChecked = settings.notificationsEnabled
        intervalSlider?.value = settings.hydrationIntervalMinutes.toFloat()
        intervalLabel?.text = getString(R.string.interval_minutes, settings.hydrationIntervalMinutes)
        darkModeSwitch?.setOnCheckedChangeListener(null)
        darkModeSwitch?.isChecked = settings.darkModeEnabled
        darkModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            // Flip the global theme immediately so the user sees feedback before leaving.
            val desiredMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
                AppCompatDelegate.setDefaultNightMode(desiredMode)
            }
            val current = prefs.getSettings()
            if (current.darkModeEnabled != isChecked) {
                prefs.saveSettings(current.copy(darkModeEnabled = isChecked))
            }
        }

        intervalSlider?.addOnChangeListener { _, value, _ ->
            intervalLabel?.text = getString(R.string.interval_minutes, value.toInt())
        }

        save.setOnClickListener {
            val sliderValue = intervalSlider?.value?.toInt()?.coerceAtLeast(15) ?: 15
            val new = Settings(
                hydrationIntervalMinutes = sliderValue,
                notificationsEnabled = notificationsSwitch?.isChecked == true,
                darkModeEnabled = darkModeSwitch?.isChecked == true
            )
            if (new.notificationsEnabled && requiresNotificationPermission() && !hasNotificationPermission()) {
                Log.w(TAG, "POST_NOTIFICATIONS missing; requesting permission before scheduling.")
                pendingSettings = new
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                persistSettings(new)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationsSwitch = null
        intervalSlider = null
        intervalLabel = null
        darkModeSwitch = null
    }

    private fun persistSettings(settings: Settings, showToast: Boolean = true) {
        prefs.saveSettings(settings)
        scheduleOrCancel(settings)
        Log.i(
            TAG,
            "Settings persisted (notifications=${settings.notificationsEnabled}, interval=${settings.hydrationIntervalMinutes}m)."
        )
        if (isDebugBuild() && settings.notificationsEnabled) {
            triggerDebugReminder()
        }
        if (showToast && settings.notificationsEnabled) {
            context?.let { ctx ->
                val message = ctx.getString(R.string.hydration_reminder_set, settings.hydrationIntervalMinutes)
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requiresNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun hasNotificationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    private fun scheduleOrCancel(settings: Settings) {
        val wm = WorkManager.getInstance(requireContext())
        val workName = "hydration_reminder"
        if (settings.notificationsEnabled) {
            // Use unique periodic work so updates replace the existing schedule cleanly.
            val req = PeriodicWorkRequestBuilder<HydrationWorker>(
                settings.hydrationIntervalMinutes.toLong().coerceAtLeast(15), TimeUnit.MINUTES
            ).build()
            wm.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.UPDATE, req)
            Log.d(TAG, "Enqueued hydration_reminder with interval=${settings.hydrationIntervalMinutes}m.")
        } else {
            wm.cancelUniqueWork(workName)
            Log.d(TAG, "Cancelled hydration_reminder work.")
        }
    }

    private fun isDebugBuild(): Boolean =
        (requireContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun triggerDebugReminder() {
        val context = context ?: return
        val wm = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<HydrationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()
        wm.enqueue(request)
        Log.d(TAG, "Queued debug hydration reminder with 10s delay for quick verification.")
    }

    private fun wrapIfLandscape(content: View): View {
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return content
        }
        val scroll = NestedScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }
        content.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        scroll.addView(content)
        return scroll
    }
}
