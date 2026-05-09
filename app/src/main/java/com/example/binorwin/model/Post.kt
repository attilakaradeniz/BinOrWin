package com.example.binorwin.model
import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,
    val title: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("bin_votes") val binVotes: Int,
    @SerializedName("win_votes") val winVotes: Int,
    @SerializedName("created_at") val createdAt: String? = null,
    val owner: UserResponse? = null,

    //counter for arguments
    @SerializedName("arguments_count") val argumentCount: Int = 0
)

// Data class for post
data class PostCreate(
    val title: String,
    @SerializedName("image_url") val imageUrl: String
)

data class UploadRespose(
    val image_url: String
)