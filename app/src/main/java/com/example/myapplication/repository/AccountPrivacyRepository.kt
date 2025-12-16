package com.example.myapplication.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AccountPrivacyRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun updateProfileVisibility(uid: String, isPublic: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(uid)
                    .update("isProfilePublic", isPublic)
                    .await()
                true
            } catch (e: Exception) {
                Log.e("PrivacyRepo", "Update Visibility Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun getProfileVisibility(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("users").document(uid).get().await()
                // Default to true if field doesn't exist
                snapshot.getBoolean("isProfilePublic") ?: true
            } catch (e: Exception) {
                Log.e("PrivacyRepo", "Get Visibility Exception: ${e.message}")
                true // Default public on error
            }
        }
    }
}