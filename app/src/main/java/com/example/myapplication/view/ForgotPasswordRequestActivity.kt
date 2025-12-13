package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.repository.AuthRepository
import com.example.myapplication.viewModel.AuthViewModel
import com.example.myapplication.viewModel.AuthViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordRequestActivity : AppCompatActivity() {

    // Use AuthViewModel (which handles AuthRepository)
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_request)

        val etEmail = findViewById<TextInputEditText>(R.id.input_email)
        val btnSend = findViewById<Button>(R.id.btn_send)
        val tvBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)
        val btnHeaderBack = findViewById<ImageView>(R.id.btn_header_back)

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Strict Email Validation
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else if (!email.matches(emailPattern.toRegex())) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.resetPassword(email)
            }
        }

        tvBackToLogin.setOnClickListener {
            finish() // Fixed: Removed Activity.
        }

        btnHeaderBack.setOnClickListener {
            finish() // Fixed: Removed Activity.
        }

        // Observer Loading State
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show()
            }
        }

        // Observer Reset Result
        viewModel.passwordResetResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                // Navigate to Confirm Activity
                val intent = Intent(this, ForgotPasswordConfirmActivity::class.java)
                startActivity(intent) // Fixed: Removed Activity.
                finish() // Fixed: Removed Activity.
            }.onFailure { exception ->
                Toast.makeText(this, "Failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
