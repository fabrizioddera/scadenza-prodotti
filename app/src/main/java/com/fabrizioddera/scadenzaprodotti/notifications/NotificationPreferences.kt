package com.fabrizioddera.scadenzaprodotti.notifications

import android.content.Context

object NotificationPreferences {
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_HOUR = "check_hour"
    private const val KEY_MINUTE = "check_minute"

    const val DEFAULT_HOUR = 8
    const val DEFAULT_MINUTE = 0

    fun getHour(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HOUR, DEFAULT_HOUR)

    fun getMinute(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_MINUTE, DEFAULT_MINUTE)

    fun save(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()
    }
}
