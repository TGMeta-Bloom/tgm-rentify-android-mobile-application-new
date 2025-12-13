// com.example.myapplication.repository.OnboardingRepository.kt
package com.example.myapplication.repository

import com.example.myapplication.model.OnboardingItem
import com.example.myapplication.utils.SharedPreferencesHelper

class OnboardingRepository(private val prefsHelper: SharedPreferencesHelper) {



    fun checkOnboardingStatus(): Boolean {
        return prefsHelper.isOnboardingCompleted()
    }

    fun markOnboardingAsCompleted() {
        prefsHelper.setOnboardingCompleted(true)
    }
}