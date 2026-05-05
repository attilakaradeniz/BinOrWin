package com.example.binorwin.network

import android.content.Context
import com.example.binorwin.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // pi (Tailscale) IP address!
    private const val BASE_URL = "http://100.112.97.88:8000/"

    // Hold a reference to our safe (TokenManager)
    private var tokenManager: TokenManager? = null

    // This must be called from MainActivity when the app starts
    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    // The Interceptor intercepts every outgoing request to attach the JWT token
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // If we have a token in the safe, stick it on the package!
        tokenManager?.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Proceed with the (now potentially authorized) request
        chain.proceed(requestBuilder.build())
    }

    // Build the OkHttpClient with our custom interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Equip Retrofit with our smart client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Helper methods to manage the token from outside (like from a ViewModel)

    // Save the token after a successful login
    fun saveAuthToken(token: String) {
        tokenManager?.saveToken(token)
    }

    // Clear the token when the user logs out
    fun clearAuthToken() {
        tokenManager?.clearToken()
    }

    // Check if the user is currently logged in
    fun isLoggedIn(): Boolean {
        return tokenManager?.getToken() != null
    }

}