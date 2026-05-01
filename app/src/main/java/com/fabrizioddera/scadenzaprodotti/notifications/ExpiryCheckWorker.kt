package com.fabrizioddera.scadenzaprodotti.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fabrizioddera.scadenzaprodotti.data.AppDatabase

class ExpiryCheckWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val products = AppDatabase.getInstance(applicationContext).productDao().getAllProductsSync()
        products.forEach { product ->
            if (product.daysUntilExpiry <= product.daysBeforeNotify) {
                NotificationHelper.showExpiryNotification(applicationContext, product)
            }
        }
        return Result.success()
    }
}
