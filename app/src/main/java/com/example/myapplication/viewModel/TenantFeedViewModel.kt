package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.TenantPost
import com.example.myapplication.repository.TenantFeedRepository
import com.google.firebase.firestore.FirebaseFirestore

class TenantFeedViewModel : ViewModel() {

    private val repository = TenantFeedRepository()
    private val _feedPosts = MutableLiveData<List<TenantPost>>()

    val feedPosts: LiveData<List<TenantPost>> get() = _feedPosts

    fun loadFeed() {
        repository.getFeedPosts { posts ->
            _feedPosts.value = posts
        }
    }

    fun updateHelpfulCount(postId: String, newCount: Int) {
        FirebaseFirestore.getInstance().collection("posts")
            .document(postId)
            .update("helpfulCount", newCount)
            .addOnFailureListener { e ->
                android.util.Log.e("ViewModel", "Error updating count", e)
            }
    }

    fun hidePost(postId: String) {
        val currentList = _feedPosts.value?.toMutableList() ?: return
        // Remove the post with the matching ID
        currentList.removeAll { it.id == postId }
        // Update the LiveData so the UI refreshes
        _feedPosts.value = currentList
    }
}