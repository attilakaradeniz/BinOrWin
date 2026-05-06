package com.example.binorwin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binorwin.model.UserCreate
import com.example.binorwin.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the different states of our authentication process
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    // Internal state that can be updated
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Public state that the UI observes
    val authState: StateFlow<AuthState> = _authState

    // Function to handle login
    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Call the API
                val response = RetrofitClient.apiService.login(username, pass)
                // Save the token to our safe (TokenManager)
                RetrofitClient.saveAuthToken(response.access_token)

                //save the username
                RetrofitClient.saveUserName(username)
                // Tell the UI it was successful
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                // If it fails (e.g., wrong password, 401 Unauthorized), send error state
                _authState.value = AuthState.Error("Login failed: Check your credentials")
            }
        }
    }

    // Function to handle signup
    fun signup(username: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val newUser = UserCreate(username, email, pass)
                // Call the API to create user
                RetrofitClient.apiService.signup(newUser)

                // If signup is successful, automatically log them in!
                login(username, pass)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Signup failed: Username might exist")
            }
        }
    }

    // Reset the state (useful when switching between login and signup screens)
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}