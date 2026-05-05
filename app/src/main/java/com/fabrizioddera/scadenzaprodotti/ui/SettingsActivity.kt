package com.fabrizioddera.scadenzaprodotti.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fabrizioddera.scadenzaprodotti.ScadenzaApp
import com.fabrizioddera.scadenzaprodotti.databinding.ActivitySettingsBinding
import com.fabrizioddera.scadenzaprodotti.notifications.NotificationPreferences
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.fabrizioddera.scadenzaprodotti.R.string.settings_title)

        updateTimeLabel()

        binding.btnPickTime.setOnClickListener { showTimePicker() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showTimePicker() {
        val hour = NotificationPreferences.getHour(this)
        val minute = NotificationPreferences.getMinute(this)

        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(getString(com.fabrizioddera.scadenzaprodotti.R.string.settings_pick_time))
            .build()
            .also { picker ->
                picker.addOnPositiveButtonClickListener {
                    NotificationPreferences.save(this, picker.hour, picker.minute)
                    updateTimeLabel()
                    (application as ScadenzaApp).scheduleExpiryCheck()
                }
                picker.show(supportFragmentManager, "time_picker")
            }
    }

    private fun updateTimeLabel() {
        val hour = NotificationPreferences.getHour(this)
        val minute = NotificationPreferences.getMinute(this)
        binding.textCurrentTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
