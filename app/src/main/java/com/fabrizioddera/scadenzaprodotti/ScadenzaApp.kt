package com.fabrizioddera.scadenzaprodotti

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fabrizioddera.scadenzaprodotti.notifications.ExpiryCheckWorker
import com.fabrizioddera.scadenzaprodotti.notifications.NotificationHelper
import com.fabrizioddera.scadenzaprodotti.notifications.NotificationPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ScadenzaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleExpiryCheck()
    }

    fun scheduleExpiryCheck() {
        val hour = NotificationPreferences.getHour(this)
        val minute = NotificationPreferences.getMinute(this)

        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delay = next.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}
