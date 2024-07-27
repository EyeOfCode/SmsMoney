package com.example.smsautoread

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val smsPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Permission granted, proceed with functionality
            } else {
                // Permission denied, handle accordingly
                Log.d("SMS", "SMS read permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }

        val openSecondPageButton: Button = findViewById(R.id.btn_config)
        openSecondPageButton.setOnClickListener {
            // Open the second activity
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun readAllSms() {
//        val smsUri = Telephony.Sms.CONTENT_URI
//        val projection = arrayOf(
//            Telephony.Sms._ID,
//            Telephony.Sms.ADDRESS,
//            Telephony.Sms.BODY,
//            Telephony.Sms.DATE
//        )
//
//        val cursor = contentResolver.query(smsUri, projection, null, null, null)
//        cursor?.use {
//            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
//            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
//            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
//            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
//
//            while (it.moveToNext()) {
//                val id = it.getString(idIndex)
//                val address = it.getString(addressIndex)
//                val body = it.getString(bodyIndex)
//                val dateMillis = it.getLong(dateIndex)
//
//                // Convert date from milliseconds to a readable format
//                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(dateMillis)
//
//                // Process or display the SMS details
//                Log.d("SMS", "ID: $id, Address: $address, Body: $body, Date: $date")
//            }
//        }
//    }
}
