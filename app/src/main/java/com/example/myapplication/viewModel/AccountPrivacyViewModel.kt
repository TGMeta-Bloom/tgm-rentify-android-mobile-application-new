package com.example.myapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.AccountPrivacyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AccountPrivacyViewModel : ViewModel() {

    private val repository = AccountPrivacyRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isProfilePublic = MutableLiveData<Boolean>()
    val isProfilePublic: LiveData<Boolean> get() = _isProfilePublic

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    fun loadPrivacySettings() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val isPublic = repository.getProfileVisibility(uid)
            _isProfilePublic.postValue(isPublic)
        }
    }

    fun updateProfileVisibility(isPublic: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        
        // Optimistic update
        _isProfilePublic.value = isPublic
        
        viewModelScope.launch {
            val success = repository.updateProfileVisibility(uid, isPublic)
            if (success) {
                _updateStatus.postValue("Privacy settings updated")
            } else {
                _updateStatus.postValue("Failed to update privacy settings")
                // Revert on failure
                _isProfilePublic.postValue(!isPublic)
            }
        }
    }
}