package com.example.myapplication.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.User
import kotlinx.coroutines.delay

/**
 * Simulated Repository for Frontend Development.
 * Uses GLOBAL mock data so changes persist across screens.
 */
class ProfileRepository {

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    companion object {

        private var globalMockUser = User(
            userId = "mock_user_123",
            firstName = "Thamasha",
            lastName = "Nethmini",
            email = "thamasha@gmail.com",
            mobileNumber = "+94763939423",
            bio = "Touring and exploring the land in design and community engagement, let's connect and grow together.",
            city = "Matara",
            role = "Tenant", // Default
            profileImageUrl = null,
            coverImageUrl = null
        )


        fun updateRole(newRole: String) {
            globalMockUser = globalMockUser.copy(role = newRole)
        }
    }

    suspend fun fetchCurrentUser() {
        delay(500)
        _userData.postValue(globalMockUser)
    }

    suspend fun updateUserProfile(user: User, imageUri: Uri?): Result<String> {
        delay(800)
        
        var updatedUser = user
        if (imageUri != null) {
            updatedUser = updatedUser.copy(profileImageUrl = imageUri.toString())
        }
        
        globalMockUser = updatedUser
        _userData.postValue(globalMockUser)
        return Result.success("Profile updated successfully (Simulated)")
    }

    suspend fun updateCoverImage(user: User, imageUri: Uri): Result<String> {
        delay(800) 

        val updatedUser = user.copy(coverImageUrl = imageUri.toString())
        globalMockUser = updatedUser
        _userData.postValue(globalMockUser)

        return Result.success("Cover image updated successfully (Simulated)")
    }
    
    fun switchRole() {
        val newRole = if (globalMockUser.role.equals("Landlord", ignoreCase = true)) "Tenant" else "Landlord"
        globalMockUser = globalMockUser.copy(role = newRole)
        _userData.postValue(globalMockUser)
    }
}