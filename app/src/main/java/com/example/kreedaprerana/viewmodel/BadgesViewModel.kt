package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Badge
import com.example.kreedaprerana.data.repository.BadgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Badges/Achievements screen.
 * Provides real-time list of all earned badges from Firestore.
 */
class BadgesViewModel : ViewModel() {

    private val repository = BadgeRepository()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    /** All badges across all athletes. */
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllBadges()
                .catch { /* silently handle */ }
                .collect { badgeList ->
                    _badges.value = badgeList
                    _isLoading.value = false
                }
        }
    }
}
