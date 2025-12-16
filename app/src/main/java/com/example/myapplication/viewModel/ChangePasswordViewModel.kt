package com.example.myapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.ChangePasswordRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    private val repository = ChangePasswordRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _verificationStatus = MutableLiveData<Result<String>>()
    val verificationStatus: LiveData<Result<String>> get() = _verificationStatus

    private val _updateStatus = MutableLiveData<Result<String>>()
    val updateStatus: LiveData<Result<String>> get() = _updateStatus
    
    private val _resetStatus = MutableLiveData<Result<String>>()
    val resetStatus: LiveData<Result<String>> get() = _resetStatus

    fun verifyCurrentPassword(password: String) {
        val user = auth.currentUser
        if (user == null) {
            _verificationStatus.postValue(Result.failure(Exception("User not logged in")))
            return
        }

        // Check if user uses Password provider
        val isPasswordProvider = user.providerData.any { it.providerId == "password" }
        if (!isPasswordProvider) {
             _verificationStatus.postValue(Result.failure(Exception("You are logged in via Google/Social. Cannot change password.")))
             return
        }

        viewModelScope.launch {
            val success = repository.reauthenticate(password)
            if (success) {
                _verificationStatus.postValue(Result.success("Verified Successfully"))
            } else {
                _verificationStatus.postValue(Result.failure(Exception("Incorrect Password")))
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            val success = repository.updatePassword(newPassword)
            if (success) {
                repository.signOut()
                _updateStatus.postValue(Result.success("Password Updated Successfully"))
            } else {
                _updateStatus.postValue(Result.failure(Exception("Failed to update password")))
            }
        }
    }

    fun sendPasswordReset() {
        viewModelScope.launch {
            val success = repository.sendPasswordResetEmail()
            if (success) {
                _resetStatus.postValue(Result.success("Reset email sent"))
            } else {
                _resetStatus.postValue(Result.failure(Exception("Failed to send reset email")))
            }
        }
    }
}