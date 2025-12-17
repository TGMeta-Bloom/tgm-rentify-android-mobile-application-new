package com.example.myapplication.model

import com.google.firebase.firestore.DocumentId

data class SavedProperty(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val title: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val rentAmount: Double = 0.0
)