package com.example.binorwin.model

// Data sent to the server to create a new user
data class UserCreate(
    val username: String,
    val email: String,
    val password: String
)

// Data received from the server after successful signup or when user data is requested
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val created_at: String
)

// Data received from the server containing the JWT token after a successful login
data class TokenResponse(
    val access_token: String,
    val token_type: String
)