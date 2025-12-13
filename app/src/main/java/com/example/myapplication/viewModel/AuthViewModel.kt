package com.example.myapplication.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.User
import com.example.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Registration Result: Success -> UID, Failure -> Exception
    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> = _registrationResult

    // Login Result: Success -> User Object (with Role), Failure -> Exception
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    // Password Reset Result: Success -> Unit, Failure -> Exception
    private val _passwordResetResult = MutableLiveData<Result<Unit>>()
    val passwordResetResult: LiveData<Result<Unit>> = _passwordResetResult

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Added context parameter to handle ImgBB upload
    // Solution 1: Changed imageUri to Uri? (Nullable)
    fun registerUser(context: Context, user: User, password: String, imageUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.registerUser(context, user, password, imageUri)
            _registrationResult.value = result
            _isLoading.value = false
        }
    }

    fun loginUser(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    // New Function for Forgot Password
    fun resetPassword(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            _passwordResetResult.value = result
            _isLoading.value = false
        }
    }
}

// Factory to create AuthViewModel with Repository dependency
class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
