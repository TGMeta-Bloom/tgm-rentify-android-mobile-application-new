package com.example.myapplication.view

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferencesHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial

class AppSettingsActivity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var btnBack: ImageView
    private lateinit var btnAbout: LinearLayout
    private lateinit var tvLight: TextView
    private lateinit var tvDark: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // FIX: Initialize SharedPreferences and set Night Mode BEFORE super.onCreate
        // This ensures the Activity context is created with the correct Theme Configuration from the start.
        prefsHelper = SharedPreferencesHelper(this)
        val savedIsDark = prefsHelper.isDarkMode()
        val mode = if (savedIsDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        
        // Only update if different to avoid loops/flickers, but ensure it's set before View creation
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        // Bind Views
        btnBack = findViewById(R.id.btn_back_settings)
        switchTheme = findViewById(R.id.switch_theme)
        btnAbout = findViewById(R.id.btn_about_app)
        tvLight = findViewById(R.id.tv_theme_light)
        tvDark = findViewById(R.id.tv_theme_dark)

        // Initialize State
        switchTheme.isChecked = savedIsDark
        updateThemeTextColors(savedIsDark)

        // Listeners
        btnBack.setOnClickListener {
            finish()
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            // 1. Save Preference
            prefsHelper.setDarkMode(isChecked)
            
            // 2. Update Text Colors (Immediate feedback)
            updateThemeTextColors(isChecked)
            
            // 3. Apply Theme
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(newMode)
            
            // 4. Recreate to apply changes
            recreate()
        }

        btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun updateThemeTextColors(isDark: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.app_blue)
        // Use White for inactive in Dark Mode, Black for inactive in Light Mode
        val inactiveColor = if (isDark) Color.WHITE else Color.BLACK

        if (isDark) {
            tvLight.setTextColor(inactiveColor) 
            tvDark.setTextColor(activeColor) 
        } else {
            tvLight.setTextColor(activeColor) 
            tvDark.setTextColor(inactiveColor)
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Rentify")
            .setMessage("Rentify is a comprehensive property management application.\n\nVersion: ${BuildConfig.VERSION_NAME}\nBuild: ${BuildConfig.VERSION_CODE}")
            .setPositiveButton("OK", null)
            .show()
    }
}