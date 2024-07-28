package com.example.smsautoread

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SmsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val logs = StringBuilder()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("config_data", Context.MODE_PRIVATE)

    override fun doWork(): Result {
        // Retrieve data from inputData
        val sender = inputData.getString("sender") ?: return Result.failure()
        val messageBody = inputData.getString("messageBody") ?: return Result.failure()

        val url = sharedPreferences.getString("url", "")
        if (url == null) {
            logs.append("SmsWorker", "URL not found in SharedPreferences")
            return Result.failure()
        }

        // Send SMS data to API
        sendSmsToApi(sender, messageBody)

        return Result.success()
    }

    private fun sendSmsToApi(sender: String, messageBody: String) {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor("token"))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://webhook.site/77985478-b816-48a8-8f0c-74b5d24a909c")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val smsData = SmsData(sender, messageBody)

        apiService.sendSmsData(smsData).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    logs.append("SmsWorker", "SMS data sent successfully")
                } else {
                    logs.append("SmsWorker", "Failed to send SMS data")
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                logs.append("SmsWorker", "Error: ${t.message}")
            }
        })
    }
}
