package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.model.User
import com.example.myapplication.network.ImgBBClient
import com.example.myapplication.utils.FileUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Registration Process
    suspend fun registerUser(context: Context, user: User, password: String, imageUri: Uri?): Result<String> {
        // Move execution to IO Thread to prevent UI Freezes during File Copy/Network
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepo", "Starting Registration. User: ${user.email}, Role: ${user.role}")

                // 0. Pre-flight Check
                if (imageUri != null && BuildConfig.
                    IMGBB_API_KEY.isEmpty()) {
                    return@withContext Result.failure(Exception("Configuration Error: ImgBB API Key is missing. Please check local.properties."))
                }

                // 1. Create User in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                val firebaseUser = authResult.user
                val uid = firebaseUser?.uid
                    ?: return@withContext Result.failure(Exception("User creation failed"))

                // --- STRICT SECURITY: Send Email Verification ---
                try {
                    firebaseUser.sendEmailVerification().await()
                    Log.d("AuthRepo", "Verification email sent to ${user.email}")
                } catch (e: Exception) {
                    Log.e("AuthRepo", "Failed to send verification email: ${e.message}")

                    // CRITICAL FIX: If email fails, DELETE the user and fail registration.
                    // This ensures the user knows something went wrong (e.g., Quota exceeded, Invalid Email)

                    try {
                        firebaseUser.delete().await()
                    } catch (deleteEx: Exception) {
                        Log.e(
                            "AuthRepo",
                            "Failed to delete user after email error: ${deleteEx.message}"
                        )
                    }

                    return@withContext Result.failure(Exception("Failed to send verification email: ${e.message}. Please try again."))
                }

                Log.d("AuthRepo", "Firebase Auth Success. UID: $uid")

                // 2. Upload Image to ImgBB (if exists)
                var downloadUrl: String? = null
                if (imageUri != null) {
                    try {
                        Log.d("AuthRepo", "Processing Image...")
                        val part = FileUtils.uriToMultipart(context, imageUri)

                        Log.d("AuthRepo", "Uploading to ImgBB...")
                        val response = ImgBBClient.api.uploadImage(
                            apiKey = BuildConfig.IMGBB_API_KEY,
                            image = part,
                            name = "profile_$uid"
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            downloadUrl =
                                response.body()?.data?.display_url ?: response.body()?.data?.url
                            Log.d("AuthRepo", "ImgBB Upload Success: $downloadUrl")
                        } else {
                            val errorMsg =
                                "ImgBB Upload Failed: ${response.code()} ${response.message()}"
                            Log.e("AuthRepo", errorMsg)
                            throw RuntimeException(errorMsg)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthRepo", "ImgBB Error: ${e.message}")
                        // Fail registration so user knows image didn't work
                        throw Exception("Image Upload Failed: ${e.message}")
                    }
                }

                // 3. Prepare User Object with UID and Image URL
                val userToSave = user.copy(
                    userId = uid,
                    profileImageUrl = downloadUrl
                )

                // 4. Save to Firestore
                firestore.collection("users").document(uid).set(userToSave).await()
                Log.d("AuthRepo", "User saved to Firestore successfully.")

                Result.success(uid)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Registration Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Login Process
    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                val uid = firebaseUser?.uid
                    ?: return@withContext Result.failure(Exception("Login failed: No UID"))

                // --- STRICT SECURITY: Check Email Verification ---
                if (!firebaseUser.isEmailVerified) {
                    // Try to resend the verification email automatically
                    try {
                        firebaseUser.sendEmailVerification().await()
                        Log.d("AuthRepo", "Resent verification email to $email")
                        auth.signOut()
                        return@withContext Result.failure(Exception("Email not verified. We sent a new link to your inbox."))
                    } catch (e: Exception) {
                        Log.e("AuthRepo", "Failed to resend verification email: ${e.message}")
                        auth.signOut()
                        // Give user the EXACT error reason (e.g. Quota Exceeded)
                        return@withContext Result.failure(Exception("Email not verified. Failed to send link: ${e.message}"))
                    }
                }

                val document = firestore.collection("users").document(uid).get().await()

                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Failed to parse user data"))
                    }
                } else {
                    Result.failure(Exception("User data not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Password Reset
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Password Reset Failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
