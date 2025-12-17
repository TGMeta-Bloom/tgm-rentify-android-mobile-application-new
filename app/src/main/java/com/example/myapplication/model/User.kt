package com.example.myapplication.model

import com.google.firebase.firestore.PropertyName

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String? = null,
    val email: String = "",
    val mobileNumber: String = "",
    val bio: String = "",
    val city: String = "",
    val address: String = "",
    val role: String = "",
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null,
    
    // Optional Contact Fields
    val instagramLink: String? = null,
    val facebookLink: String? = null,
    val whatsappLink: String? = null,
    val websiteLink: String? = null,
    
    // Annotation ensures Firestore uses "isPhonePublic" instead of stripping "is" to "phonePublic"
    @get:PropertyName("isPhonePublic")
    val isPhonePublic: Boolean = true,

    // New Profile Visibility Field
    @get:PropertyName("isProfilePublic")
    val isProfilePublic: Boolean = true
)