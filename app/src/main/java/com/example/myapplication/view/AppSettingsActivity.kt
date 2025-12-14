package com.example.myapplication.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class AppSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        val btnBack: ImageView = findViewById(R.id.btn_back_settings)
        btnBack.setOnClickListener { finish() }
    }
}
