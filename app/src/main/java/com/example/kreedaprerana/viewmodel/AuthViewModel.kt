package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Coach
import com.example.kreedaprerana.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Authentication (Login, Signup, Logout) and the current Coach profile.
 */
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _currentCoach = MutableStateFlow<Coach?>(null)
    val currentCoach: StateFlow<Coach?> = _currentCoach.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    init {
        // Automatically fetch coach profile if already logged in on startup
        if (repository.isUserLoggedIn()) {
            fetchCurrentCoach()
        }
    }

    /**
     * Checks if there's an active session directly.
     */
    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _authSuccess.value = false

            val result = repository.loginCoach(email.trim(), password)
            result.onSuccess {
                fetchCurrentCoach()
                _authSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Login failed. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun signup(
        fullName: String,
        schoolName: String,
        phoneNumber: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (fullName.isBlank() || schoolName.isBlank() || phoneNumber.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Please fill in all fields."
            return
        }
        if (password != confirmPassword) {
            _error.value = "Passwords do not match."
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _authSuccess.value = false

            val coach = Coach(
                fullName = fullName.trim(),
                schoolName = schoolName.trim(),
                phoneNumber = phoneNumber.trim(),
                email = email.trim()
            )

            val result = repository.registerCoach(coach, password)
            result.onSuccess {
                _currentCoach.value = it
                _authSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Signup failed. Please try again."
            }

            _isLoading.value = false
        }
    }

    private fun fetchCurrentCoach() {
        viewModelScope.launch {
            val result = repository.getCurrentCoach()
            result.onSuccess { coach ->
                _currentCoach.value = coach
            }.onFailure {
                _currentCoach.value = null
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentCoach.value = null
        _authSuccess.value = false
    }

    fun resetAuthSuccess() {
        _authSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
