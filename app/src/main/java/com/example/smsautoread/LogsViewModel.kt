package com.example.smsautoread

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogsViewModel : ViewModel() {
    private val _logs = MutableLiveData<String>()
    val logs: LiveData<String> get() = _logs

    init {
        _logs.value = ""
    }

    fun addLog(log: String) {
        _logs.value = "${_logs.value}\n$log"
    }

    fun setLogs(logs: String) {
        _logs.value = logs
    }
}
