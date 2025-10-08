package com.example.serenity_mad_le3.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.serenity_mad_le3.util.NotificationHelper

class HydrationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        NotificationHelper.showHydrationReminder(applicationContext)
        return Result.success()
    }
}

