package com.example.smsautoread

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver(){
    //TODO check name sms addressIndex
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            if (context != null) {
                val sharedPreferencesData: SharedPreferences = context.getSharedPreferences("config_data", Context.MODE_PRIVATE)
                val active = sharedPreferencesData.getBoolean("active", false)
                val header = sharedPreferencesData.getString("header", "")
                if(active) {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val pdus = bundle.get("pdus") as Array<*>?
                        val messages = mutableListOf<SmsMessage>()
                        pdus?.forEach { pdu ->
                            val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                            messages.add(smsMessage)
                        }

                        // Combine the messages if needed
                        val fullMessage = messages.joinToString(separator = "") { it.messageBody }
                        val sender = messages[0].displayOriginatingAddress
                        val timestampMillis = messages[0].timestampMillis

                        // Format the timestamp
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val dateString = dateFormat.format(Date(timestampMillis))

                        if(header.toString() == "" || header.toString() == sender) {
                            scheduleSmsWork(context, sender, fullMessage, dateString)
                            sendUpdateBroadcast(context, sender, fullMessage, dateString)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleSmsWork(context: Context, sender: String, messageBody: String, date: String) {
        // Create input data for the worker
        val inputData = Data.Builder()
            .putString("sender", sender)
            .putString("messageBody", messageBody)
            .putString("date", date)
            .build()

        // Create a OneTimeWorkRequest
        val workRequest = OneTimeWorkRequest.Builder(SmsWorker::class.java)
            .setInputData(inputData)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun sendUpdateBroadcast(context: Context, sender: String, messageBody: String, date: String) {
        val intent = Intent("com.example.smsautoread.SMS_RECEIVED")
        intent.putExtra("sender", sender)
        intent.putExtra("messageBody", messageBody)
        intent.putExtra("date", date)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
