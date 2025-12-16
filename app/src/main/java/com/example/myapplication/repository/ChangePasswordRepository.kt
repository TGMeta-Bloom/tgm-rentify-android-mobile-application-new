package com.example.myapplication.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class ChangePasswordRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun reauthenticate(password: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            Log.e("ChangePasswordRepo", "Reauth Error: ${e.message}")
            false
        }
    }

    suspend fun updatePassword(newPassword: String): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            user.updatePassword(newPassword).await()
            true
        } catch (e: Exception) {
            Log.e("ChangePasswordRepo", "Update Password Error: ${e.message}")
            false
        }
    }

    suspend fun sendPasswordResetEmail(): Boolean {
        return try {
            val email = auth.currentUser?.email ?: return false
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            Log.e("ChangePasswordRepo", "Reset Email Error: ${e.message}")
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}