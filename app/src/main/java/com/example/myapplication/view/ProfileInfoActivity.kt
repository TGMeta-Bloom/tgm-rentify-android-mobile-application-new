package com.example.myapplication.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class ProfileInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        val btnBack: ImageView = findViewById(R.id.btn_back_profile_info)
        btnBack.setOnClickListener { finish() }
    }
}
