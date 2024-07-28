package com.example.smsautoread

import android.content.Context
import android.content.SharedPreferences
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun readAllSms(context: Context): String {
    val sharedPreferencesData: SharedPreferences = context.getSharedPreferences("config_data", Context.MODE_PRIVATE)
    val active = sharedPreferencesData.getBoolean("active", false)
    val logs = StringBuilder()

    if(active) {
        val smsUri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        //TODO check name sms addressIndex
        val cursor = context.contentResolver.query(smsUri, projection, null, null, null)
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val dateMillis = it.getLong(dateIndex)

                // Convert date from milliseconds to a readable format
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date(dateMillis)
                )

                // Process or display the SMS details
                logs.append("ID: $id\nAddress: $address\nBody: $body\nDate: $date\n\n")
            }
        } ?: logs.append("No SMS messages found.")
    } else {
        logs.append("No SMS messages found.")
    }
    return logs.toString()
}
