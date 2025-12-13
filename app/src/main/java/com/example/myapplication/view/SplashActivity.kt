package com.example.myapplication.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Apply Theme Preference
        val sharedPreferences = getSharedPreferences("THEME_PREF", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false)

        if (sharedPreferences.contains("is_dark_mode")) {
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // === Animation Logic ===
        val logo = findViewById<ImageView>(R.id.iv_app_logo)
        val appName = findViewById<TextView>(R.id.tv_app_name)

        // 1. Set Initial State (Invisible & Scaled Down)
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        logo.translationY = 100f // Start slightly lower

        appName.alpha = 0f
        appName.translationY = 50f

        // 2. Animate Logo (Pop up and Fade in)
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator()) // Adds a nice bounce effect
            .setDuration(1200)
            .start()

        // 3. Animate Text (Fade in and slide up)
        appName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(500) // Wait for logo to start
            .setDuration(1000)
            .start()

        // === Navigation Logic ===
        lifecycleScope.launch {
            delay(2500) // Wait for splash animation

            val auth = FirebaseAuth.getInstance()
            val prefsHelper = SharedPreferencesHelper(this@SplashActivity)
            
            // Check if user is already logged in
            if (auth.currentUser != null && prefsHelper.getAuthToken() != null) {
                // User is logged in, go straight to the main dashboard
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // User is not logged in, decide between Onboarding and Role Selection
                val nextActivityClass = if (prefsHelper.isOnboardingCompleted()) {
                    RoleSelectionActivity::class.java
                } else {
                    OnboardingActivity::class.java
                }
                startActivity(Intent(this@SplashActivity, nextActivityClass))
            }
            
            finish() // Prevent user from returning to the splash screen
        }
    }
}
