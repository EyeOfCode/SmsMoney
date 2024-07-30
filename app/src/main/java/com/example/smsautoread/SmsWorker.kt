package com.example.smsautoread

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SmsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val sharedPreferencesData: SharedPreferences =
        context.getSharedPreferences("config_data", Context.MODE_PRIVATE)
    private val sharedPreferencesSmsLogs: SharedPreferences = context.getSharedPreferences("worker_logs", Context.MODE_PRIVATE)
    private val logs = StringBuilder()
    private val handler = Handler(Looper.getMainLooper())

    override fun doWork(): Result {
        // Retrieve data from inputData
        val sender = inputData.getString("sender") ?: return Result.failure()
        val messageBody = inputData.getString("messageBody") ?: return Result.failure()
        val date = inputData.getString("date") ?: return Result.failure()

        // Send SMS data to API
        val url = sharedPreferencesData.getString("url", "")
        val token = sharedPreferencesData.getString("token", "")
        val header = sharedPreferencesData.getString("header", "")
        if (url != null && url !== "") {
            if(header.toString() == "" || header.toString() == sender) {
                val newLog = "Sender: $sender\nMessage: $messageBody\nDate: $date\n"
                logs.append(newLog)
                sendSmsToApi(url, token, sender, messageBody, date, logs)
            }else{
                logs.append("Sender not match header\n")
            }
        }

        logs.append("###################\n\n")
        saveLogsToPreferences(logs.toString())

        return Result.success()
    }

    private fun sendSmsToApi(url: String, token: String?, sender: String, messageBody: String, date: String, logs: StringBuilder) {
        try {
            val httpClientBuilder  = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
            token?.let {
                httpClientBuilder .addInterceptor(AuthInterceptor(it))
            }
            val httpClient = httpClientBuilder.build()

            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val smsData = SmsData(sender, messageBody, date)

            apiService.sendSmsData(smsData).enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("status_sms", "SMS data sent successfully\n\n")
                        handler.post {
                            Toast.makeText(applicationContext, "SMS data sent successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("status_sms","Failed to send SMS data\n\n")
                        handler.post {
                            Toast.makeText(applicationContext, "Failed to send SMS data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    Log.d("status_sms","WorkerSMS Error: ${t.message}\n\n")
                    handler.post {
                        Toast.makeText(applicationContext, "WorkerSMS Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        } catch (t: Throwable) {
            Log.d("status_sms","WorkerSMS Error: worker not running\n ${t.message}\n\n")
            handler.post {
                Toast.makeText(applicationContext, "WorkerSMS Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveLogsToPreferences(logData: String) {
        val editor = sharedPreferencesSmsLogs.edit()
        val existingLogs = sharedPreferencesSmsLogs.getString("logs", "") ?: ""
        editor.putString("logs", existingLogs + logData)
        editor.apply()
    }
}
