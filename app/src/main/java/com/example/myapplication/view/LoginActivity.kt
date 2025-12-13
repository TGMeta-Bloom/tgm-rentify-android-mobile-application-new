package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.repository.AuthRepository
import com.example.myapplication.utils.SharedPreferencesHelper
import com.example.myapplication.viewModel.AuthViewModel
import com.example.myapplication.viewModel.AuthViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private var selectedRoleFromIntent: String? = null
    private lateinit var prefsHelper: SharedPreferencesHelper
    
    // ViewModel setup
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefsHelper = SharedPreferencesHelper(this)
        selectedRoleFromIntent = intent.getStringExtra("selected_role")

        val etEmail = findViewById<TextInputEditText>(R.id.input_email)
        val etPassword = findViewById<TextInputEditText>(R.id.input_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.text_create_one)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val tvForgotPassword = findViewById<TextView>(R.id.text_forgot_password)

        setupObservers()

        btnBack.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // Reset errors
            etEmail.error = null
            etPassword.error = null

            // Input Validation
            
            // 1. Check Empty Email
            if (email.isEmpty()) {
                etEmail.error = "Email address is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // 2. Check for missing '@'
            if (!email.contains("@")) {
                etEmail.error = "Email must contain '@' symbol"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // 3. Check for valid domain format (using standard Android Patterns)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email format"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // 4. Check Empty Password
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Trigger Login via ViewModel if all checks pass
            viewModel.loginUser(email, password)
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java).apply {
                putExtra("selected_role", selectedRoleFromIntent)
            }
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordRequestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        // Observe Loading State
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Login Result
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                // 1. Save Session Data Locally
                if (user.userId.isNotEmpty()) {
                    prefsHelper.saveAuthToken(user.userId)
                    prefsHelper.saveUserId(user.userId)
                }
                
                // Role is crucial
                if (user.role.isNotEmpty()) {
                    prefsHelper.saveUserRole(user.role)
                }

                if (user.firstName.isNotEmpty()) {
                    prefsHelper.saveUserName("${user.firstName} ${user.lastName}")
                }
                if (user.email.isNotEmpty()) {
                    prefsHelper.saveUserEmail(user.email)
                }

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                // 2. Navigate to Main Activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }.onFailure { exception ->
                // Handle Error Message Logic
                val rawMessage = exception.message ?: ""
                val friendlyMessage = when {
                    rawMessage.contains("network", ignoreCase = true) -> "Network error. Please check your connection."
                    rawMessage.contains("password", ignoreCase = true) || rawMessage.contains("credential", ignoreCase = true) -> "Incorrect email or password."
                    rawMessage.contains("user", ignoreCase = true) || rawMessage.contains("identifier", ignoreCase = true) -> "Account not found."
                    rawMessage.contains("blocked", ignoreCase = true) -> "Too many failed attempts. Try again later."
                    else -> "Login failed. Please check your details."
                }
                
                Toast.makeText(this, friendlyMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}
