package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.TenantPost
import com.example.myapplication.repository.TenantFeedRepository

class TenantFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenantFeedRepository(application.applicationContext)
    private val _feedPosts = MutableLiveData<List<TenantPost>>()

    val feedPosts: LiveData<List<TenantPost>> get() = _feedPosts

    init {
        loadFeed()
    }

    fun loadFeed() {
        repository.getFeedPosts { posts ->
            _feedPosts.value = posts
        }
    }

    fun updateHelpfulCount(postId: String, newCount: Int) {
        // Use repository now!
        repository.updateHelpfulCount(postId, newCount)
    }

    fun hidePost(postId: String) {
        val currentList = _feedPosts.value?.toMutableList() ?: return
        currentList.removeAll { it.id == postId }
        _feedPosts.value = currentList
    }
}