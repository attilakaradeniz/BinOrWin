package com.example.binorwin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binorwin.model.Argument
import com.example.binorwin.model.ArgumentCreate
import com.example.binorwin.model.Post
import com.example.binorwin.network.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Reactive list for the feed
    val posts = mutableStateListOf<Post>()

    // Reactive list to hold the arguments for the currently selected post
    var currentArguments = mutableStateListOf<Argument>()
        private set

    // State to track if the list is currently being refreshed via swipe
    var isRefreshing by mutableStateOf(false)
        private set

    init {
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
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun vote(postId: Int, voteType: String) {
        viewModelScope.launch {
            try {
                val updatedPost = RetrofitClient.apiService.voteOnPost(postId, voteType)
                val index = posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    posts[index] = updatedPost
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fetch arguments for a specific post and update the UI state
    fun fetchArguments(postId: Int) {
        viewModelScope.launch {
            try {
                currentArguments.clear()
                val fetchedArgs = RetrofitClient.apiService.getArgumentsForPost(postId)
                currentArguments.addAll(fetchedArgs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Send a new argument to the backend
    fun createArgument(postId: Int, actionType: String, content: String) {
        viewModelScope.launch {
            try {
                val newArg = ArgumentCreate(actionType = actionType, content = content)
                RetrofitClient.apiService.createArgumentForPost(postId, newArg)
                // Refresh the arguments list immediately after posting
                fetchArguments(postId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}