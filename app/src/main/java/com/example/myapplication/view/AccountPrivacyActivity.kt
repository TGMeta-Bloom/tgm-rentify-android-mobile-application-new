package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.viewModel.AccountPrivacyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

class AccountPrivacyActivity : AppCompatActivity() {

    private val viewModel: AccountPrivacyViewModel by viewModels()

    private lateinit var btnBack: ImageView
    private lateinit var switchVisibility: SwitchMaterial
    private lateinit var tvVisibilityStatus: TextView
    private lateinit var btnChangePassword: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_privacy)

        // Bind Views
        btnBack = findViewById(R.id.btn_back_privacy)
        switchVisibility = findViewById(R.id.switch_visibility)
        tvVisibilityStatus = findViewById(R.id.tv_visibility_status)
        btnChangePassword = findViewById(R.id.btn_change_password)

        // Setup Listeners
        btnBack.setOnClickListener {
            finish()
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Switch Logic
        switchVisibility.setOnCheckedChangeListener { _, isChecked ->
            // Update UI immediately for responsiveness
            updateStatusText(isChecked)
            // Call ViewModel
            viewModel.updateProfileVisibility(isChecked)
        }

        // Observe Data
        viewModel.isProfilePublic.observe(this) { isPublic ->
            // Avoid triggering listener loop if value is same
            if (switchVisibility.isChecked != isPublic) {
                switchVisibility.isChecked = isPublic
                updateStatusText(isPublic)
            } else {
                // Ensure text is correct on initial load
                updateStatusText(isPublic)
            }
        }

        viewModel.updateStatus.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Load Initial Data
        viewModel.loadPrivacySettings()
    }

    private fun updateStatusText(isPublic: Boolean) {
        if (isPublic) {
            tvVisibilityStatus.text = "Public"
            tvVisibilityStatus.setTextColor(resources.getColor(R.color.app_blue, theme))
        } else {
            tvVisibilityStatus.text = "Private"
            tvVisibilityStatus.setTextColor(resources.getColor(R.color.grey_700, theme))
        }
    }
}