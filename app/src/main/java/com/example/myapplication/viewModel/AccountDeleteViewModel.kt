package com.example.myapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.AccountDeleteRepository
import kotlinx.coroutines.launch

class AccountDeleteViewModel : ViewModel() {

    private val repository = AccountDeleteRepository()

    private val _deleteStatus = MutableLiveData<Result<String>>()
    val deleteStatus: LiveData<Result<String>> get() = _deleteStatus

    fun deleteAccount() {
        viewModelScope.launch {
            val success = repository.deleteAccount()
            if (success) {
                _deleteStatus.postValue(Result.success("Account deleted successfully"))
            } else {
                _deleteStatus.postValue(Result.failure(Exception("Failed to delete account. Please log out and log in again.")))
            }
        }
    }
}