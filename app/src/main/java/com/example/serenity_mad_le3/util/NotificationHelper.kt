package com.example.serenity_mad_le3.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.serenity_mad_le3.MainActivity
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.SerenityApp

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    fun showHydrationReminder(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS permission missing; hydration reminder skipped.")
            return
        }
        if (!manager.areNotificationsEnabled()) {
            Log.w(TAG, "System notifications disabled; hydration reminder skipped.")
            return
        }

        // Open the app when the user taps the notification so they can update progress immediately.
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, SerenityApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.hydration_title))
            .setContentText(context.getString(R.string.hydration_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Reuse the same notification id so the reminder stays singular rather than stacking.
        manager.notify(1001, builder.build())
        Log.i(TAG, "Hydration reminder notification dispatched (id=1001).")
    }
}
