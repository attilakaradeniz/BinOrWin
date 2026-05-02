package com.example.binorwin.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Raspberry Pi's Tailscale IP
    private const val BASE_URL = "http://100.112.97.88:8000/"

    // Lazily initialize the API service so it's only built when first needed
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Kotlin Data Classes
            .build()
            .create(ApiService::class.java)
    }
}