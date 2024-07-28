package com.example.smsautoread

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LogsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
