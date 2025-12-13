package com.example.myapplication.viewModel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val _resetResult = MutableLiveData<Result<String>>()
    val resetResult: LiveData<Result<String>> = _resetResult

    fun sendResetLink(email: String) {
        viewModelScope.launch {
            // Simulate network loading
            delay(2000)

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _resetResult.value = Result.success("Reset link sent successfully")
            } else {
                _resetResult.value = Result.failure(Exception("Please enter a valid email address"))
            }
        }
    }
}