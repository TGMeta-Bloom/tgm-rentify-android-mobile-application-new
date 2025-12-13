package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class ForgotPasswordConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_confirm)

        val tvBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)
        val btnHeaderBack = findViewById<ImageView>(R.id.btn_header_back)
        
        // This screen is now a static confirmation screen.
        // It simply tells the user: "Check your email for the reset link."
        
        tvBackToLogin.setOnClickListener {
            // Navigate back to Login and clear stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent) // Corrected: removed Activity.
            finish()              // Corrected: removed Activity.
        }
        
        btnHeaderBack.setOnClickListener {
            // Navigate back to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent) // Corrected
            finish()              // Corrected
        }
    }
}
