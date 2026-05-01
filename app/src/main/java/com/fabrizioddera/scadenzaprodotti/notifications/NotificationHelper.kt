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
            "Scadenze prodotti",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifiche per prodotti in scadenza"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun showExpiryNotification(context: Context, product: Product) {
        val days = product.daysUntilExpiry
        val message = when {
            days < 0 -> "${product.name} è scaduto ${-days} giorni fa"
            days == 0L -> "${product.name} scade oggi!"
            else -> "${product.name} scade tra $days giorni"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Prodotto in scadenza")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(product.id, notification)
    }
}
