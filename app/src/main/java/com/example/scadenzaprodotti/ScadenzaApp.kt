package com.example.scadenzaprodotti

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.scadenzaprodotti.notifications.ExpiryCheckWorker
import com.example.scadenzaprodotti.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

class ScadenzaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleExpiryCheck()
    }

    private fun scheduleExpiryCheck() {
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
