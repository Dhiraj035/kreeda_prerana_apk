package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Athlete
import com.example.kreedaprerana.data.repository.AthleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for athlete management. Shared between AddAthleteScreen and AthleteListScreen.
 * Handles add, update, delete operations and exposes a real-time athlete list.
 */
class AthleteViewModel : ViewModel() {

    private val repository = AthleteRepository()

    private val _athletes = MutableStateFlow<List<Athlete>>(emptyList())
    /** Real-time list of all athletes from Firestore. */
    val athletes: StateFlow<List<Athlete>> = _athletes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    /** True while a network operation is in progress. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error message from the latest failed operation, or null. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    /** Becomes true briefly after a successful save operation. */
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        // Start listening to athlete collection in real-time
        viewModelScope.launch {
            repository.getAthletes()
                .catch { e ->
                    _error.value = e.message ?: "Failed to load athletes"
                }
                .collect { athleteList ->
                    _athletes.value = athleteList
                }
        }
    }

    /**
     * Adds a new athlete to Firestore with all profile fields.
     */
    fun addAthlete(
        name: String,
        age: Int,
        sport: String,
        gender: String = "",
        schoolName: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            val athlete = Athlete(
                name = name,
                age = age,
                sport = sport,
                gender = gender,
                schoolName = schoolName,
                createdAt = System.currentTimeMillis()
            )
            val result = repository.addAthlete(athlete)

            result.onSuccess {
                _saveSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to save athlete"
            }

            _isLoading.value = false
        }
    }

    /**
     * Deletes an athlete from Firestore by their ID.
     */
    fun deleteAthlete(athleteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.deleteAthlete(athleteId)
            result.onFailure { e ->
                _error.value = e.message ?: "Failed to delete athlete"
            }

            _isLoading.value = false
        }
    }

    /**
     * Updates an existing athlete's details in Firestore.
     */
    fun updateAthlete(athlete: Athlete) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.updateAthlete(athlete)
            result.onFailure { e ->
                _error.value = e.message ?: "Failed to update athlete"
            }

            _isLoading.value = false
        }
    }

    /** Resets the saveSuccess flag after the UI has consumed it. */
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    /** Clears the current error message. */
    fun clearError() {
        _error.value = null
    }
}
