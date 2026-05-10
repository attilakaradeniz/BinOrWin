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
import com.example.binorwin.model.TokenResponse
import com.example.binorwin.model.UploadRespose
import com.example.binorwin.model.UserCreate
import com.example.binorwin.model.UserResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.DELETE
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part

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

    @POST("arguments/{argument_id}/like")
    suspend fun likeArgument(
        @Path("argument_id") argumentId: Int
    ): Argument

    // Send user data as a JSON body to create a new account
    @POST("signup")
    suspend fun signup(@Body user: UserCreate): UserResponse

    // FastAPI's OAuth2 implementation strictly expects form data (FormUrlEncoded) for login, NOT a JSON body!
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    // Update an existing argument
    @PUT("arguments/{argument_id}")
    suspend fun updateArgument(
        @Path("argument_id") argumentId: Int,
        @Body argument: ArgumentCreate
    ): Argument

    // Delete an argument
    @DELETE("arguments/{argument_id}")
    suspend fun deleteArgument(
        @Path("argument_id") argumentId: Int
    )

    @Multipart
    @POST("upload/")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadRespose

    // Delete a post
    @DELETE("posts/{post_id}")
    suspend fun deletePost(
        @Path("post_id") postId: Int
    ): retrofit2.Response<Unit> // instead of Any

    // Update a post title
    @PUT("posts/{post_id}")
    suspend fun updatePost(
        @Path("post_id") postId: Int,
        @Body postUpdate: com.example.binorwin.model.PostUpdate
    ): retrofit2.Response<Unit>
}


