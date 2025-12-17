package com.example.myapplication.model

data class ImgBBResponse(
    val data: ImgBBData?,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val id: String?,
    val title: String?,
    val url_viewer: String?,
    val url: String?,
    val display_url: String?,
    val delete_url: String?
)
