package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.model.TenantPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TenantFeedRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getFeedPosts(onResult: (List<TenantPost>) -> Unit) {
        // Log that we are STARTING the fetch
        Log.d("FIREBASE_DEBUG", "Attempting to fetch posts...")

        db.collection("posts") // Ensure this matches your database EXACTLY
            .orderBy("timestamp", Query.Direction.DESCENDING) // remove this line temporarily if you suspect date issues
            .get()
            .addOnSuccessListener { result ->
                // Log how many documents were found
                Log.d("FIREBASE_DEBUG", "Success! Found ${result.size()} documents.")

                if (result.isEmpty) {
                    Log.w("FIREBASE_DEBUG", "The 'posts' collection is empty or does not exist.")
                }

                val postList = result.toObjects(TenantPost::class.java)
                onResult(postList)
            }
            .addOnFailureListener { exception ->
                // Log the SPECIFIC error message
                Log.e("FIREBASE_DEBUG", "Error fetching data: ${exception.message}", exception)
                onResult(emptyList())
            }
    }
}