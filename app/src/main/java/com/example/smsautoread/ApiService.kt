package com.example.smsautoread

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("sms/receive")
    fun sendSmsData(@Body smsData: SmsData): Call<ResponseBody>
}