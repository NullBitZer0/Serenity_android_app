package com.example.serenity_mad_le3.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.serenity_mad_le3.util.NotificationHelper

class HydrationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        // Delegate to the helper so the reminder payload stays consistent with other entry points.
        NotificationHelper.showHydrationReminder(applicationContext)
        return Result.success()
    }
}
