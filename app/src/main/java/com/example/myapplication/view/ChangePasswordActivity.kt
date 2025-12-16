package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.viewModel.ChangePasswordViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordActivity : AppCompatActivity() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    private lateinit var etCurrent: TextInputEditText
    private lateinit var btnVerify: MaterialButton
    private lateinit var etNew: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var tvForgot: TextView
    private lateinit var btnBack: ImageView

    private var isVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // Bind Views
        etCurrent = findViewById(R.id.et_current_password)
        btnVerify = findViewById(R.id.btn_verify_password)
        etNew = findViewById(R.id.et_new_password)
        etConfirm = findViewById(R.id.et_confirm_password)
        btnSave = findViewById(R.id.btn_save_password)
        tvForgot = findViewById(R.id.tv_forgot_current_password)
        btnBack = findViewById(R.id.btn_back_change_pw)

        // Listeners
        btnBack.setOnClickListener { finish() }

        btnVerify.setOnClickListener {
            val currentPw = etCurrent.text.toString().trim()
            if (currentPw.isNotEmpty()) {
                viewModel.verifyCurrentPassword(currentPw)
            } else {
                etCurrent.error = "Enter current password"
            }
        }

        btnSave.setOnClickListener {
            if (!isVerified) {
                Toast.makeText(this, "Please verify current password first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPw = etNew.text.toString().trim()
            val confirmPw = etConfirm.text.toString().trim()

            if (newPw.length < 6) {
                etNew.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                etConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }

            viewModel.updatePassword(newPw)
        }

        tvForgot.setOnClickListener {
            viewModel.sendPasswordReset()
        }

        // Observers
        viewModel.verificationStatus.observe(this) { result ->
            result.onSuccess {
                isVerified = true
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                btnVerify.isEnabled = false // Disable verify to show state
                btnVerify.text = "Verified"
            }.onFailure {
                isVerified = false
                Toast.makeText(this, "Verification Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                // Navigate to Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure {
                Toast.makeText(this, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.resetStatus.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}