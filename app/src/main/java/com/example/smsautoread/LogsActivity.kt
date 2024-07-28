package com.example.smsautoread

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LogsActivity : AppCompatActivity() {
    private lateinit var logTextView: TextView

    private fun getLogsFromPreferences(): String {
        val sharedPreferences: SharedPreferences = getSharedPreferences("sms_logs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("logs", "No logs available.") ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsUpdateReceiver)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        displayLogs()

        val goBackButton: Button = findViewById(R.id.btn_back)
        goBackButton.setOnClickListener {
            finish()
        }
    }

    private fun displayLogs() {
        logTextView = findViewById(R.id.logTextView)
        LocalBroadcastManager.getInstance(this).registerReceiver(smsUpdateReceiver, IntentFilter("com.example.smsautoread.SMS_RECEIVED"))
        val logs = getLogsFromPreferences()
        logTextView.text = logs
    }

    private val smsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val logs = getLogsFromPreferences()
            logTextView.text = logs
        }
    }
}