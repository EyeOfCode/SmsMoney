package com.example.smsautoread

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class ConfigActivity  : AppCompatActivity() {
    private lateinit var inputUrl: EditText
    private lateinit var inputHeader: EditText
    private lateinit var toggleActive: SwitchMaterial
    private lateinit var updateButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val goBackButton: Button = findViewById(R.id.btn_back)
        goBackButton.setOnClickListener {
            // Finish this activity and return to the first activity
            finish()
        }

        val openTestLogsPageButton: Button = findViewById(R.id.btn_test)
        openTestLogsPageButton.setOnClickListener {
            val intent = Intent(this, TestLogsActivity::class.java)
            startActivity(intent)
        }

        inputUrl = findViewById(R.id.input_url)
        inputHeader = findViewById(R.id.input_header)
        toggleActive = findViewById(R.id.toggle_active)
        updateButton = findViewById(R.id.btn_update)

        sharedPreferences = getSharedPreferences("config_data", Context.MODE_PRIVATE)

        loadSavedData()

        updateButton.setOnClickListener {
            saveData()
            Toast.makeText(this, "Config updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        editor.putString("url", inputUrl.text.toString())
        editor.putString("header", inputHeader.text.toString())
        editor.putBoolean("active", toggleActive.isChecked)
        editor.apply()
    }

    private fun loadSavedData() {
        val url = sharedPreferences.getString("url", "")
        val header = sharedPreferences.getString("header", "")
        val isActive = sharedPreferences.getBoolean("active", false)

        inputUrl.setText(url)
        inputHeader.setText(header)
        toggleActive.isChecked = isActive
    }
}