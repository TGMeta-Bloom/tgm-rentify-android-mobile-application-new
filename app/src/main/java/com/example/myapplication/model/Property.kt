package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Property(
    val propertyId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val rentAmount: Double = 0.0,
    val propertyType: String = "",
    val imageUrls: List<String>? = null,
    val status: String = "",
    val contactNumber: String = ""
) : Parcelable