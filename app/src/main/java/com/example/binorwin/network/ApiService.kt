package com.example.binorwin.network

import com.example.binorwin.model.Argument
import com.example.binorwin.model.ArgumentCreate
import com.example.binorwin.model.Post
import com.example.binorwin.model.PostCreate
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// defining all API endpoints in FastAPI
interface ApiService {
    // Fetch the list of posts
    @GET("posts/")
    suspend fun getPosts(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): List<Post>

    // Send a new post to the server
    @POST("posts/")
    suspend fun createPost(@Body post: PostCreate): Post

    // Send a vote (bin or win) for a specific post
    @POST("posts/{post_id}/vote")
    suspend fun voteOnPost(
        @Path("post_id") postId: Int,
        @Query("vote_type") voteType: String
    ): Post

    // Fetch all arguments (comments) for a specific post
    @GET("posts/{post_id}/arguments")
    suspend fun getArgumentsForPost(@Path("post_id") postId: Int): List<Argument>

    // Send a new argument to a specific post
    @POST("posts/{post_id}/arguments")
    suspend fun createArgumentForPost(
        @Path("post_id") postId: Int,
        @Body argument: ArgumentCreate
    ): Argument
}