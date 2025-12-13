package com.example.myapplication.model

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val bio: String = "",
    val city: String = "",
    val role: String = "",
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null
)