package com.example.smsautoread

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//TODO
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                for (pdu in pdus) {
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = smsMessage.displayOriginatingAddress
                    val messageBody = smsMessage.messageBody

                    Toast.makeText(context, "New SMS received: $messageBody", Toast.LENGTH_SHORT).show()
                    Log.d("SmsReceiver", "Sender: $sender")
                    Log.d("SmsReceiver", "Message: $messageBody")

                    // Call your API here
                    sendSmsToApi(sender, messageBody)
                }
            }
        }
    }

    private fun sendSmsToApi(sender: String, messageBody: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://webhook.site/77985478-b816-48a8-8f0c-74b5d24a909c")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val smsData = SmsData(sender, messageBody)

        val call = apiService.sendSmsData(smsData)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("SmsReceiver", "SMS data sent successfully")
                } else {
                    Log.e("SmsReceiver", "Failed to send SMS data")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("SmsReceiver", "Error: ${t.message}")
            }
        })
    }
}
