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
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

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

    // Function to delete an argument and refresh the list
    fun deleteArgument(postId: Int, argumentId: Int) {
        viewModelScope.launch {
            try {
                com.example.binorwin.network.RetrofitClient.apiService.deleteArgument(argumentId)
                // Refresh the arguments list after deleting
                fetchArguments(postId)
            } catch (e: Exception) {
                // Handle error (e.g., show a toast)
            }
        }
    }

    // fnction to update an argument and refresh the list
    fun updateArgument(postId: Int, argumentId: Int, actionType: String, content: String) {
        viewModelScope.launch {
            try {
                val updatedArg = com.example.binorwin.model.ArgumentCreate(actionType, content)
                com.example.binorwin.network.RetrofitClient.apiService.updateArgument(argumentId, updatedArg)
                // Refresh the arguments list after editing
                fetchArguments(postId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // function to like/unlike an argument
    fun likeArgument(postId: Int, argumentId: Int) {
        viewModelScope.launch {
            try {
                com.example.binorwin.network.RetrofitClient.apiService.likeArgument(argumentId)
                // Refresh the arguments list so the heart counter updates immediately!
                fetchArguments(postId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createPost(title: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                val newPost = com.example.binorwin.model.PostCreate(title = title, imageUrl = imageUrl)
                com.example.binorwin.network.RetrofitClient.apiService.createPost(newPost)
                refreshPosts() // to show the latest
            } catch (e: Exception) {
                // TO DO:
            }
        }
    }

    // file gen from URI
    fun uploadImageAndCreatePost(context: android.content.Context, title: String, imageUri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val file = getFileFromUri(context, imageUri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    // send to API
                    val response = com.example.binorwin.network.RetrofitClient.apiService.uploadImage(body)

                    // create post
                    val newPost = com.example.binorwin.model.PostCreate(title = title, imageUrl = response.image_url)
                    com.example.binorwin.network.RetrofitClient.apiService.createPost(newPost)

                    // if succeess refresh list
                    refreshPosts()
                    // succeess
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                // android std log (Logcat)
                android.util.Log.e("UploadError", "Yükleme Hatası: ${e.message}", e)

                // info to UI screen
                android.widget.Toast.makeText(context, "Hata: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()

                // stop progress wheel!
                onResult(false)
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }
    // Function to delete a post
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                val response = com.example.binorwin.network.RetrofitClient.apiService.deletePost(postId)
                if (response.isSuccessful) {
                    // if delete success refresh posts
                    refreshPosts() //
                } else {
                    // if ERROR (eg: 403, 404, 422) then log
                    val errorDetail = response.errorBody()?.string()
                    android.util.Log.e("DeleteError", "FastAPI msg: $errorDetail")
                }
            } catch (e: Exception) {
                android.util.Log.e("DeleteError", "couldnt reach the server: ${e.message}")
            }
        }
    }

    // Function to update post title
    fun updatePostTitle(postId: Int, newTitle: String) {
        viewModelScope.launch {
            try {
                val updateData = com.example.binorwin.model.PostUpdate(title = newTitle)
                val response = com.example.binorwin.network.RetrofitClient.apiService.updatePost(postId, updateData)
                if (response.isSuccessful) {
                    // Refresh the feed to show the new title
                    refreshPosts()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditError", "Error updating post: ${e.message}")
            }
        }
    }


}