package com.example.smsautoread

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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

    override fun doWork(): Result {
        Log.d("WorkerSMS","Worker start\n")
        // Retrieve data from inputData
        val sender = inputData.getString("sender") ?: return Result.failure()
        val messageBody = inputData.getString("messageBody") ?: return Result.failure()
        val date = inputData.getString("date") ?: return Result.failure()

        val newLog = "Sender: $sender\nMessage: $messageBody\nDate: $date\n\n"
        Log.d("WorkerSMS", newLog)

        // Send SMS data to API
        val url = sharedPreferencesData.getString("url", null)
        val token = sharedPreferencesData.getString("token", null)
        if (url != null) {
            sendSmsToApi(url, token, sender, messageBody, date)
        }

        saveLogsToPreferences(newLog)

        return Result.success()
    }

    private fun sendSmsToApi(url: String, token: String?, sender: String, messageBody: String, date: String) {
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
                        Log.d("WorkerSMS","SMS data sent successfully\n\n")
                    } else {
                        Log.d("WorkerSMS","Failed to send SMS data\n\n")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                    Log.d("WorkerSMS","Error: ${t.message}")
                }
            })
        } catch (t: Throwable) {
            Log.d("WorkerSMS","Error: worker not running\n ${t.message}")
        }
    }

    private fun saveLogsToPreferences(logData: String) {
        val editor = sharedPreferencesSmsLogs.edit()
        val existingLogs = sharedPreferencesSmsLogs.getString("logs", "") ?: ""
        editor.putString("logs", existingLogs + logData)
        editor.apply()
    }
}
