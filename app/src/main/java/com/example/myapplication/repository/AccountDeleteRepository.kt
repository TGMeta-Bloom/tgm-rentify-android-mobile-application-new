package com.example.myapplication.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AccountDeleteRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun deleteAccount(): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val uid = user.uid

            // 1. Delete User Data from Firestore
            firestore.collection("users").document(uid).delete().await()
            
            // 2. Delete User Authentication
            user.delete().await()
            
            true
        } catch (e: Exception) {
            Log.e("AccountDeleteRepo", "Delete Error: ${e.message}")
            // Typically throws FirebaseAuthRecentLoginRequiredException if session is old
            false
        }
    }
}