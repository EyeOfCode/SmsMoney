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

class SmsReceiver : BroadcastReceiver(){
    private val logs = StringBuilder()

    override fun onReceive(context: Context?, intent: Intent?) {
        logs.append("New SMS\n")
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            if (context != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle["pdus"] as Array<*>
                    for (pdu in pdus) {
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val sender = smsMessage.displayOriginatingAddress
                        val messageBody = smsMessage.messageBody

                        logs.append("SMS received: $messageBody\n")
                        logs.append("Sender: $sender\n")
                        logs.append("Message: $messageBody\n\n")

                        // Schedule work using WorkManager
                        scheduleSmsWork(context, sender, messageBody)
                        sendUpdateBroadcast(context, sender, messageBody)
                    }
                }
            }
        }
        context?.let { saveLogsToPreferences(it, logs.toString()) }
    }

    private fun saveLogsToPreferences(context: Context, logData: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("sms_logs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("logs", logData)
        editor.apply()
    }

    private fun scheduleSmsWork(context: Context, sender: String, messageBody: String) {
        // Create input data for the worker
        val inputData = Data.Builder()
            .putString("sender", sender)
            .putString("messageBody", messageBody)
            .build()

        // Create a OneTimeWorkRequest
        val workRequest = OneTimeWorkRequest.Builder(SmsWorker::class.java)
            .setInputData(inputData)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun sendUpdateBroadcast(context: Context, sender: String, messageBody: String) {
        val intent = Intent("com.example.smsautoread.SMS_RECEIVED")
        intent.putExtra("sender", sender)
        intent.putExtra("messageBody", messageBody)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
