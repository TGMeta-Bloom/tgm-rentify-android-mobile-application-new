package com.example.myapplication.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferencesHelper
import com.example.myapplication.viewModel.AccountDeleteViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AccountDeleteActivity : AppCompatActivity() {

    private val viewModel: AccountDeleteViewModel by viewModels()
    private lateinit var btnBack: ImageView
    private lateinit var btnConfirm: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_delete)

        btnBack = findViewById(R.id.btn_back_delete)
        btnConfirm = findViewById(R.id.btn_confirm_delete)

        btnBack.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            showConfirmationDialog()
        }

        viewModel.deleteStatus.observe(this) { result ->
            result.onSuccess {
                // Clear local data
                val prefsHelper = SharedPreferencesHelper(this)
                prefsHelper.clear()
                
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                
                // Navigate to Role Selection Page
                val intent = Intent(this, RoleSelectionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone and you will lose all data.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAccount()
            }
            .show()
    }
}