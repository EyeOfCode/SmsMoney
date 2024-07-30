package com.example.smsautoread

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LogsActivity : AppCompatActivity() {
    private lateinit var logTextView: TextView
    private val logsViewModel: LogsViewModel by viewModels { LogsViewModelFactory(this) }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        logTextView = findViewById(R.id.logTextView)

        loadLogsFromPreferences()

        // Observe changes to logs and update UI
        logsViewModel.logs.observe(this) { logs ->
            logTextView.text = logs
        }

        // Register the local broadcast receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(smsUpdateReceiver, IntentFilter("com.example.smsautoread.SMS_RECEIVED"))

        val goBackButton: Button = findViewById(R.id.btn_back)
        goBackButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsUpdateReceiver)
    }

    private val smsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the logs when a new SMS is received
            val sender = intent?.getStringExtra("sender") ?: "Unknown Sender"
            val messageBody = intent?.getStringExtra("messageBody") ?: "No Message"
            val date = intent?.getStringExtra("date") ?: "No Message"
            val newLog = "New SMS from Sender: $sender\nMessage: $messageBody\nDate: $date"
            logsViewModel.addLog(newLog)
            logsViewModel.addLog("###################\n")
            scrollToBottom()
        }
    }

    private fun loadLogsFromPreferences() {
        val sharedPreferencesSmsLogs: SharedPreferences = getSharedPreferences("worker_logs", Context.MODE_PRIVATE)
        val logs = sharedPreferencesSmsLogs.getString("logs", "") ?: ""
        logsViewModel.setLogs(logs)
        logTextView.text = logs
        scrollToBottom()
    }

    private fun scrollToBottom() {
        findViewById<ScrollView>(R.id.scroll_view).post {
            findViewById<ScrollView>(R.id.scroll_view).fullScroll(View.FOCUS_DOWN)
        }
    }
}
