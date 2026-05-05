package com.example.binorwin.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    // Create a private SharedPreferences file specifically for our app
    private val prefs: SharedPreferences = context.getSharedPreferences("binorwin_prefs", Context.MODE_PRIVATE)

    // Save the JWT token securely
    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    // Retrieve the JWT token (returns null if not logged in)
    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    // Delete the token (used when the user logs out)
    fun clearToken() {
        prefs.edit().remove("jwt_token").apply()
    }
}