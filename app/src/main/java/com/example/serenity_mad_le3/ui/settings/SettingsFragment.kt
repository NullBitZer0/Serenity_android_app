package com.example.serenity_mad_le3.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Settings
import com.example.serenity_mad_le3.workers.HydrationWorker
import com.google.android.material.slider.Slider
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = Prefs(requireContext())
        val settings = prefs.getSettings()

        val switch = view.findViewById<Switch>(R.id.switchNotifications)
        val slider = view.findViewById<Slider>(R.id.sliderInterval)
        val label = view.findViewById<TextView>(R.id.textInterval)
        val save = view.findViewById<Button>(R.id.btnSaveSettings)

        switch.isChecked = settings.notificationsEnabled
        slider.value = settings.hydrationIntervalMinutes.toFloat()
        label.text = getString(R.string.interval_minutes, settings.hydrationIntervalMinutes)

        slider.addOnChangeListener { _, value, _ ->
            label.text = getString(R.string.interval_minutes, value.toInt())
        }

        save.setOnClickListener {
            val new = Settings(
                hydrationIntervalMinutes = slider.value.toInt().coerceAtLeast(15),
                notificationsEnabled = switch.isChecked
            )
            prefs.saveSettings(new)
            scheduleOrCancel(new)
        }
    }

    private fun scheduleOrCancel(settings: Settings) {
        val wm = WorkManager.getInstance(requireContext())
        val workName = "hydration_reminder"
        if (settings.notificationsEnabled) {
            val req = PeriodicWorkRequestBuilder<HydrationWorker>(
                settings.hydrationIntervalMinutes.toLong().coerceAtLeast(15), TimeUnit.MINUTES
            ).build()
            wm.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.UPDATE, req)
        } else {
            wm.cancelUniqueWork(workName)
        }
    }
}

