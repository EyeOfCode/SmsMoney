package com.example.smsautoread

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TestLogsActivity : AppCompatActivity() {
    private val logs = StringBuilder()
    private lateinit var logTextView: TextView

    private val smsPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Permission granted, proceed with functionality
            } else {
                // Permission denied, handle accordingly
                logTextView = findViewById(R.id.logTextView)
                logs.append("SMS read permission denied")
                logTextView.text = logs
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_logs)

        val goBackButton: Button = findViewById(R.id.btn_back)
        goBackButton.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }else{
            val logTextView: TextView = findViewById(R.id.logTextView)
            val logs = readAllSms(this)
            logTextView.text = logs
        }
    }
}