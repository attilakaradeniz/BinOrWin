package com.example.binorwin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binorwin.model.Post
import com.example.binorwin.network.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // A reactive list that the UI will observe
    val posts = mutableStateListOf<Post>()

    // State to track if the list is currently being refreshed via swipe
    var isRefreshing by mutableStateOf(false)
        private set

    init {
        // Fetch posts automatically when the ViewModel is created
        refreshPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                val fetchedPosts = RetrofitClient.apiService.getPosts()
                posts.clear()
                posts.addAll(fetchedPosts)
            } catch (e: Exception) {
                // Handle errors (like network being down) here
                e.printStackTrace()
            } finally {
                // Always stop the refreshing animation regardless of success or failure
                isRefreshing = false
            }
        }
    }

    fun vote(postId: Int, voteType: String) {
        viewModelScope.launch {
            try {
                val updatedPost = RetrofitClient.apiService.voteOnPost(postId, voteType)
                // Find the post in our list and update its vote counts
                val index = posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    posts[index] = updatedPost
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}