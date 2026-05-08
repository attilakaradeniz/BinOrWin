package com.example.binorwin.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("binorwin_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    // save username
    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    // to get username
    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}