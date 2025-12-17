package com.example.myapplication.model

data class LibraryArticle(
    val title: String,
    val body: String
)

data class LibraryCategory(
    val title: String,
    val iconRes: Int,
    val colorHex: String, // e.g., "#FF9800"
    val articles: List<LibraryArticle>
)