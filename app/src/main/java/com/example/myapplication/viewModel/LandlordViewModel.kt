package com.example.myapplication.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Property
import com.example.myapplication.model.User
import com.example.myapplication.repository.LandlordRepository
import kotlinx.coroutines.launch
import java.io.File

class LandlordViewModel : ViewModel() {

    private val repository = LandlordRepository()

    ///Trigger to reload data(after login)
    private val _refreshTrigger = MutableLiveData<Boolean>()

    ///Original LiveData from Repository, refreshed via switchMap (Use extension function switchMap directly on LiveData)
    val landlordProperties: LiveData<List<Property>> = _refreshTrigger.switchMap {
        repository.getLandlordProperties()
    }

    ///Current Filter State
    private val _currentFilter = MutableLiveData("All")

    ///Filtered List for Dashboard
    val filteredLandlordProperties = MediatorLiveData<List<Property>>().apply {
        addSource(landlordProperties) { properties ->
            value = filterList(properties, _currentFilter.value)
        }
        addSource(_currentFilter) { filter ->
            value = filterList(landlordProperties.value, filter)
        }
    }

    /// Current User Data
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        ///Initial load
        refresh()
        fetchCurrentUser()
    }

    fun refresh() {
        _refreshTrigger.value = true
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        repository.getCurrentUser { user ->
            _currentUser.postValue(user)
        }
    }

    private fun filterList(list: List<Property>?, filter: String?): List<Property> {
        if (list == null) return emptyList()
        if (filter.isNullOrEmpty() || filter == "All") return list
        return list.filter { it.propertyType.equals(filter, ignoreCase = true) }
    }

    fun setFilter(filter: String) {
        _currentFilter.value = filter
    }

    ///Loading State
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> get() = _isProcessing

    ///Error Handling
    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> get() = _errorEvent

    ///Success Event
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> get() = _operationSuccess


    ///Add or Update a Property
    fun saveProperty(property: Property, isNew: Boolean) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                if (isNew) {
                    repository.addProperty(property)
                } else {
                    repository.updateProperty(property)
                }
                _operationSuccess.value = true
                _errorEvent.value = null
            } catch (e: Exception) {
                _errorEvent.value = "Failed to save property: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isProcessing.value = false
            }
        }
    }


    ///Delete a Property
    fun deleteProperty(propertyId: String) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                repository.deleteProperty(propertyId)
                _operationSuccess.value = true
                _errorEvent.value = null
            } catch (e: Exception) {
                _errorEvent.value = "Failed to delete property: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // UPDATED: Now takes a File and uses ImgBB via Repository
    fun uploadImage(imageFile: File, onResult: (String?) -> Unit) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val downloadUrl = repository.uploadPropertyImage(imageFile)
                onResult(downloadUrl)
                _errorEvent.value = null
            } catch (e: Exception) {
                _errorEvent.value = "ImgBB Upload Failed: ${e.message}"
                onResult(null)
            } finally {
                _isProcessing.value = false
            }
        }
    }


    ///Helper to reset events after they are consumed by the UI
    fun clearError() {
        _errorEvent.value = null
    }

    fun clearSuccess() {
        _operationSuccess.value = false
    }

    // Consolidated helper for creation if needed, but standardizing on uploadImage + saveProperty is fine.
    // I'll keep this as a convenience wrapper if the fragment wants to use it.
    fun handlePropertyCreation(propertyDetails: Property, selectedImageFile: File) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val imageUrl = repository.uploadPropertyImage(selectedImageFile)
                val propertyWithImage = propertyDetails.copy(imageUrls = listOf(imageUrl))
                repository.addProperty(propertyWithImage)
                _operationSuccess.value = true
                _errorEvent.value = null
            } catch (e: Exception) {
                _errorEvent.value = "Property creation failed: ${e.message}"
                _operationSuccess.value = false
                Log.e("LandlordVM", "Property creation failed", e)
            } finally {
                _isProcessing.value = false
            }
        }
    }
}