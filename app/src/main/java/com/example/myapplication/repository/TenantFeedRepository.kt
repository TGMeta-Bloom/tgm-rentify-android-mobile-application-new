package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.model.TenantPost
import com.example.myapplication.model.User
import com.example.myapplication.network.ImgBBClient
import com.example.myapplication.utils.FileUtils
import com.example.myapplication.BuildConfig
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class TenantFeedRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

    // ADD POST (NOW USING IMGBB VIA COROUTINES)
    fun addPost(
        caption: String,
        imageUri: Uri?,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val postId = db.collection("posts").document().id

        // Launch a coroutine for the network operation
        CoroutineScope(Dispatchers.IO).launch {
            if (imageUri != null) {
                try {
                    // Use FileUtils to convert URI to a Multipart part
                    val imagePart = FileUtils.uriToMultipart(context, imageUri)

                    // Call the suspend function from your friend's API interface
                    val response = ImgBBClient.api.uploadImage(
                        apiKey = BuildConfig.IMGBB_API_KEY,
                        image = imagePart
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        val imageUrl = response.body()?.data?.url ?: ""
                        // Success: Save post data to Firestore
                        savePostToFirestore(postId, caption, imageUrl, user, onComplete)
                    } else {
                        // Failure
                        onComplete(false, "Image upload failed: ${response.message()}")
                    }
                } catch (e: Exception) {
                    onComplete(false, "Upload error: ${e.message}")
                }
            } else {
                // No Image, save directly
                savePostToFirestore(postId, caption, "", user, onComplete)
            }
        }
    }

    // SAVE POST TO FIRESTORE (Remains the same)
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

    // GET FEED POSTS (Remains the same)
    fun getFeedPosts(onResult: (List<TenantPost>) -> Unit) {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("Repository", "Listen failed.", e)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val posts = snapshot.documents.mapNotNull { doc ->
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
                        null
                    }
                }
                onResult(posts)
            }
    }
}