package com.example.myapplication.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class TenantPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val timestamp: Date? = null,
    val postImageUrl: String = "",
    val caption: String = "",
    var helpfulCount: Int = 0,
    var isHelpful: Boolean = false,

    // This field tracks the button state (Blue/Gray) locally
    @get:Exclude
    var isHelpfulClicked: Boolean = false

)

