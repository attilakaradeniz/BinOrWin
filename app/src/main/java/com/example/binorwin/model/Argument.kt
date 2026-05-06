package com.example.binorwin.model
import com.google.gson.annotations.SerializedName

// Data class representing a comment/argument received from API
data class Argument(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("action_type") val actionType: String,
    @SerializedName("created_at") val createdAt: String? = null,
    val content: String,
    // user who wrote this argument.
    val owner: UserResponse? = null
)

// Data class used when send a new comment to API
data class ArgumentCreate(
    @SerializedName("action_type") val actionType: String,
    val content: String
)
