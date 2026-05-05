package com.fabrizioddera.scadenzaprodotti.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.fabrizioddera.scadenzaprodotti.R
import com.fabrizioddera.scadenzaprodotti.data.Product

object NotificationHelper {
    private const val CHANNEL_ID = "expiry_channel"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun showExpiryNotification(context: Context, product: Product) {
        val days = product.daysUntilExpiry
        val message = when {
            days < 0 -> context.getString(R.string.notification_expired, product.name, (-days).toInt())
            days == 0L -> context.getString(R.string.notification_expires_today, product.name)
            else -> context.getString(R.string.notification_expires_in, product.name, days.toInt())
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(product.id, notification)
    }
}
