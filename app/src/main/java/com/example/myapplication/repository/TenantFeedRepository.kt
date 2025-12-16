package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.model.TenantPost
import com.example.myapplication.model.User
import com.example.myapplication.network.ImgBBClient
import com.example.myapplication.utils.FileUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TenantFeedRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

    // --- 1. ADD POST ---
    fun addPost(
        caption: String,
        imageUri: Uri?,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val postId = db.collection("posts").document().id

        CoroutineScope(Dispatchers.IO).launch {
            if (imageUri != null) {
                try {
                    val imagePart = FileUtils.uriToMultipart(context, imageUri)
                    val response = ImgBBClient.api.uploadImage(
                        apiKey = BuildConfig.IMGBB_API_KEY,
                        image = imagePart
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        val imageUrl = response.body()?.data?.url ?: ""
                        savePostToFirestore(postId, caption, imageUrl, user, onComplete)
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("UploadDebug", "ImgBB Failed. Code: $errorCode, Body: $errorBody")
                        onComplete(false, "Upload failed: $errorCode - $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e("UploadDebug", "Exception during upload", e)
                    onComplete(false, "Upload error: ${e.message}")
                }
            } else {
                savePostToFirestore(postId, caption, "", user, onComplete)
            }
        }
    }

    private fun savePostToFirestore(
        postId: String,
        caption: String,
        imageUrl: String,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val fullName = "${user.firstName} ${user.lastName}".trim()

        val post = hashMapOf(
            "id" to postId,
            "userId" to user.userId,
            "userName" to fullName,
            "userAvatarUrl" to (user.profileImageUrl ?: ""),
            "caption" to caption,
            "postImageUrl" to imageUrl,
            "timestamp" to Timestamp.now(),
            "helpfulCount" to 0,
            "isHelpfulClicked" to false
        )

        db.collection("posts").document(postId).set(post)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.message) }
    }

    // --- 2. GET FEED POSTS ---
    fun getFeedPosts(onResult: (List<TenantPost>) -> Unit) {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("Repository", "Listen failed.", e)
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val posts = parseSnapshotToPosts(snapshot)
                onResult(posts)
            }
    }

    // --- 3. GET USER POSTS ---
    fun getUserPosts(userId: String, onResult: (List<TenantPost>) -> Unit) {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(parseSnapshotToPosts(snapshot))
            }
            .addOnFailureListener { e ->
                Log.e("Repository", "Error getting user posts", e)
                onResult(emptyList())
            }
    }

    // --- 4. DELETE POST ---
    fun deletePost(postId: String, onResult: (Boolean) -> Unit) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                Log.e("Repository", "Error deleting post", it)
                onResult(false)
            }
    }

    // --- 5. UPDATE HELPFUL COUNT (New Addition) ---
    fun updateHelpfulCount(postId: String, newCount: Int) {
        db.collection("posts").document(postId)
            .update("helpfulCount", newCount)
            .addOnFailureListener { e ->
                Log.e("Repository", "Error updating helpful count", e)
            }
    }

    // --- HELPER: Parse Snapshot ---
    private fun parseSnapshotToPosts(snapshot: com.google.firebase.firestore.QuerySnapshot): List<TenantPost> {
        return snapshot.documents.mapNotNull { doc ->
            try {
                val id = doc.getString("id") ?: doc.id
                val userId = doc.getString("userId") ?: ""
                val userName = doc.getString("userName") ?: "Unknown"
                val userAvatarUrl = doc.getString("userAvatarUrl") ?: ""
                val caption = doc.getString("caption") ?: ""
                val postImageUrl = doc.getString("postImageUrl") ?: ""
                val helpfulCount = doc.getLong("helpfulCount")?.toInt() ?: 0
                val timestamp = doc.getTimestamp("timestamp")?.toDate()

                TenantPost(
                    id = id,
                    userId = userId,
                    userName = userName,
                    userAvatarUrl = userAvatarUrl,
                    caption = caption,
                    postImageUrl = postImageUrl,
                    timestamp = timestamp,
                    helpfulCount = helpfulCount
                )
            } catch (e: Exception) {
                Log.e("Repository", "Error parsing post document: ${doc.id}", e)
                null
            }
        }
    }
}