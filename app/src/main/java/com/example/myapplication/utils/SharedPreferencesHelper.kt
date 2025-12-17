package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        // MATCHING KEY
        private const val PREFS_NAME = "AppPrefs" 
        
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        // Backend / Authentication Keys
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role" // this matches logic in LoginActivity ("user_role")
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        
        // Theme
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
    }

    /**
     * Sets the status of the onboarding completion.
     * @param completed true if onboarding is completed, false otherwise.
     */
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    /**
     * Checks if the onboarding has been completed.
     * @return true if onboarding is completed, false otherwise.
     */
    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    // ----------------------------------------------------------------
    // Authentication & Session Management
    // ----------------------------------------------------------------

    /**
     * Saves the Authentication Token.
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    /**
     * Retrieves the Authentication Token.
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Saves the User Role (e.g., "Landlord", "Tenant").
     * Critical for directing the user to the correct dashboard.
     */
    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply()
    }

    /**
     * Retrieves the User Role.
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    /**
     * Saves the User ID (UID).
     */
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }

    /**
     * Retrieves the User ID.
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    /**
     * Saves the User Email (for caching).
     */
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    /**
     * Retrieves the User Email.
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Saves the User Name (for caching).
     */
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    /**
     * Retrieves the User Name.
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    /**
     * Checks if the user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    // ----------------------------------------------------------------
    // Theme Management
    // ----------------------------------------------------------------
    fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false) // Default to Light
    }

    /**
     * Clears session data (Logout) but KEEPS Onboarding status.
     * Call this when the user logs out.
     */
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
    }

    /**
     * Clears all data from the shared preferences (including onboarding).
     * Use this only for a hard reset of the app.
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    // Add these inside SharedPreferencesHelper class

    fun getProfileImageUrl(): String? {
        return sharedPreferences.getString("profile_image_url", null)
    }

    fun saveProfileImageUrl(url: String) {
        sharedPreferences.edit().putString("profile_image_url", url).apply()
    }
}