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

class ProfileRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun getUserDetails(uid: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(uid).get().await()
                val user = document.toObject(User::class.java)
                // Ensure userId matches the document ID
                user?.copy(userId = document.id)
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Error fetching user details: ${e.message}")
                null
            }
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Basic search by first name (Case sensitive in standard Firestore)
                // For better search, use Algolia or a normalized 'searchKey' field
                val snapshot = firestore.collection("users")
                    .whereGreaterThanOrEqualTo("firstName", query)
                    .whereLessThanOrEqualTo("firstName", query + "\uf8ff")
                    .get()
                    .await()
                snapshot.toObjects(User::class.java)
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Search Exception: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun updateUserProfile(context: Context, user: User, imageUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.IMGBB_API_KEY.isEmpty()) {
                    Log.e("ProfileRepo", "ImgBB API Key is missing")
                    return@withContext false
                }

                // Upload to ImgBB
                val part = FileUtils.uriToMultipart(context, imageUri)
                val response = ImgBBClient.api.uploadImage(BuildConfig.IMGBB_API_KEY, part, "profile_${user.userId}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val downloadUrl = response.body()?.data?.display_url ?: response.body()?.data?.url
                    
                    if (downloadUrl != null) {
                        // Update Firestore
                        firestore.collection("users").document(user.userId)
                            .update("profileImageUrl", downloadUrl).await()
                        return@withContext true
                    }
                }
                Log.e("ProfileRepo", "ImgBB Upload Failed: ${response.message()}")
                false
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Profile Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun updateCoverImage(context: Context, user: User, imageUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.IMGBB_API_KEY.isEmpty()) {
                    return@withContext false
                }

                val part = FileUtils.uriToMultipart(context, imageUri)
                val response = ImgBBClient.api.uploadImage(BuildConfig.IMGBB_API_KEY, part, "cover_${user.userId}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val downloadUrl = response.body()?.data?.display_url ?: response.body()?.data?.url
                    
                    if (downloadUrl != null) {
                        firestore.collection("users").document(user.userId)
                            .update("coverImageUrl", downloadUrl).await()
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Cover Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun removeProfileImage(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(uid).update("profileImageUrl", null).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Remove Profile Image Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun removeCoverImage(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(uid).update("coverImageUrl", null).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Remove Cover Image Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun updateUserRole(uid: String, newRole: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(uid).update("role", newRole).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Role Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun updateContactDetails(uid: String, instagram: String?, website: String?, isPhonePublic: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mapOf(
                    "instagramLink" to instagram,
                    "websiteLink" to website,
                    "isPhonePublic" to isPhonePublic
                )
                firestore.collection("users").document(uid).update(updates).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Contact Info Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun updateSocialLinks(uid: String, instagram: String?, facebook: String?, whatsapp: String?, website: String?, isPhonePublic: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mapOf(
                    "instagramLink" to instagram,
                    "facebookLink" to facebook,
                    "whatsappLink" to whatsapp,
                    "websiteLink" to website,
                    "isPhonePublic" to isPhonePublic
                )
                firestore.collection("users").document(uid).update(updates).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Social Links Exception: ${e.message}")
                false
            }
        }
    }

    suspend fun updatePersonalDetails(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mapOf(
                    "firstName" to user.firstName,
                    "lastName" to user.lastName,
                    "username" to user.username,
                    "bio" to user.bio,
                    "mobileNumber" to user.mobileNumber,
                    "address" to user.address,
                    "city" to user.city
                )
                firestore.collection("users").document(user.userId).update(updates).await()
                true
            } catch (e: Exception) {
                Log.e("ProfileRepo", "Update Personal Details Exception: ${e.message}")
                false
            }
        }
    }
}