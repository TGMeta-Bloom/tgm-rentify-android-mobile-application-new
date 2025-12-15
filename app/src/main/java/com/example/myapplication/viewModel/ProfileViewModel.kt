package com.example.myapplication.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.User
import com.example.myapplication.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Use the specific ProfileRepository logic
    private val repository = ProfileRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> get() = _userProfile

    private val _updateResult = MutableLiveData<Result<String>>()
    val updateResult: LiveData<Result<String>> get() = _updateResult

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> get() = _searchResults

    fun loadProfileData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    val user = repository.getUserDetails(uid)
                    _userProfile.postValue(user)
                } catch (e: Exception) {
                    _userProfile.postValue(null)
                }
            }
        }
    }

    // Function to load any user's profile by ID
    fun loadOtherUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserDetails(userId)
                _userProfile.postValue(user)
            } catch (e: Exception) {
                _userProfile.postValue(null)
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val results = repository.searchUsers(query)
            _searchResults.postValue(results)
        }
    }

    fun updateProfile(user: User, uri: Uri) {
        viewModelScope.launch {
            try {
                val success = repository.updateUserProfile(getApplication(), user, uri)
                if (success) {
                    val updatedUser = repository.getUserDetails(user.userId)
                    _userProfile.postValue(updatedUser)
                    _updateResult.postValue(Result.success("Profile photo updated"))
                } else {
                    _updateResult.postValue(Result.failure(Exception("Failed to update profile")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun updateCoverImage(user: User, uri: Uri) {
        viewModelScope.launch {
            try {
                val success = repository.updateCoverImage(getApplication(), user, uri)
                if (success) {
                    val updatedUser = repository.getUserDetails(user.userId)
                    _userProfile.postValue(updatedUser)
                    _updateResult.postValue(Result.success("Cover photo updated"))
                } else {
                    _updateResult.postValue(Result.failure(Exception("Failed to update cover")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun removeProfilePhoto() {
        val currentUser = _userProfile.value ?: return
        val optimisticUser = currentUser.copy(profileImageUrl = null)
        _userProfile.value = optimisticUser

        viewModelScope.launch {
            try {
                val success = repository.removeProfileImage(currentUser.userId)
                if (success) {
                    _updateResult.postValue(Result.success("Profile photo removed"))
                } else {
                    _userProfile.postValue(currentUser)
                    _updateResult.postValue(Result.failure(Exception("Failed to remove profile photo")))
                }
            } catch (e: Exception) {
                _userProfile.postValue(currentUser)
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun removeCoverPhoto() {
        val currentUser = _userProfile.value ?: return
        val optimisticUser = currentUser.copy(coverImageUrl = null)
        _userProfile.value = optimisticUser

        viewModelScope.launch {
            try {
                val success = repository.removeCoverImage(currentUser.userId)
                if (success) {
                    _updateResult.postValue(Result.success("Cover photo removed"))
                } else {
                    _userProfile.postValue(currentUser)
                    _updateResult.postValue(Result.failure(Exception("Failed to remove cover photo")))
                }
            } catch (e: Exception) {
                _userProfile.postValue(currentUser)
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun switchRole() {
        val currentUser = _userProfile.value ?: return
        val newRole = if (currentUser.role.equals("Landlord", ignoreCase = true)) "Tenant" else "Landlord"
        val optimisticUser = currentUser.copy(role = newRole)
        _userProfile.value = optimisticUser
        
        viewModelScope.launch {
            try {
                val success = repository.updateUserRole(currentUser.userId, newRole)
                if (success) {
                    _updateResult.postValue(Result.success("Role switched to $newRole"))
                } else {
                    _userProfile.postValue(currentUser)
                    _updateResult.postValue(Result.failure(Exception("Failed to switch role")))
                }
            } catch (e: Exception) {
                _userProfile.postValue(currentUser)
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun updateContactInfo(instagram: String?, website: String?, isPhonePublic: Boolean) {
        val currentUser = _userProfile.value ?: return
        val optimisticUser = currentUser.copy(
            instagramLink = instagram,
            websiteLink = website,
            isPhonePublic = isPhonePublic
        )
        _userProfile.value = optimisticUser

        viewModelScope.launch {
            try {
                val success = repository.updateContactDetails(currentUser.userId, instagram, website, isPhonePublic)
                if (success) {
                    _updateResult.postValue(Result.success("Contact info updated"))
                } else {
                    _updateResult.postValue(Result.failure(Exception("Failed to update contact info")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun updateSocialLinks(instagram: String?, facebook: String?, whatsapp: String?, website: String?, isPhonePublic: Boolean) {
        val currentUser = _userProfile.value ?: return
        val optimisticUser = currentUser.copy(
            instagramLink = instagram,
            facebookLink = facebook,
            whatsappLink = whatsapp,
            websiteLink = website,
            isPhonePublic = isPhonePublic
        )
        _userProfile.value = optimisticUser

        viewModelScope.launch {
            try {
                val success = repository.updateSocialLinks(currentUser.userId, instagram, facebook, whatsapp, website, isPhonePublic)
                if (success) {
                    _updateResult.postValue(Result.success("Social links updated"))
                } else {
                    _updateResult.postValue(Result.failure(Exception("Failed to update social links")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            }
        }
    }

    fun updatePersonalDetails(user: User) {
        _userProfile.value = user
        viewModelScope.launch {
            try {
                val success = repository.updatePersonalDetails(user)
                if (success) {
                    _updateResult.postValue(Result.success("Personal info updated"))
                } else {
                    _updateResult.postValue(Result.failure(Exception("Failed to update personal info")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            }
        }
    }
}