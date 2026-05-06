package com.example.binorwin.model
import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,
    val title: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("bin_votes") val binVotes: Int,
    @SerializedName("win_votes") val winVotes: Int,
    @SerializedName("created_at") val createdAt: String? = null,
    val owner: UserResponse? = null
)

// Data class for post
data class PostCreate(
    val title: String,
    @SerializedName("image_url") val imageUrl: String
)
