package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Athlete
import com.example.kreedaprerana.data.model.Badge
import com.example.kreedaprerana.data.model.Performance
import com.example.kreedaprerana.data.repository.AthleteRepository
import com.example.kreedaprerana.data.repository.BadgeRepository
import com.example.kreedaprerana.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Athlete Profile screen.
 * Loads a specific athlete's details, performance history, and earned badges.
 */
class AthleteProfileViewModel : ViewModel() {

    private val athleteRepository = AthleteRepository()
    private val performanceRepository = PerformanceRepository()
    private val badgeRepository = BadgeRepository()

    private val _athlete = MutableStateFlow<Athlete?>(null)
    val athlete: StateFlow<Athlete?> = _athlete.asStateFlow()

    private val _performances = MutableStateFlow<List<Performance>>(emptyList())
    val performances: StateFlow<List<Performance>> = _performances.asStateFlow()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Loads all data for a specific athlete.
     * Call this when navigating to the profile screen.
     */
    fun loadAthlete(athleteId: String) {
        if (athleteId.isBlank()) return

        _isLoading.value = true

        // Load athlete details
        viewModelScope.launch {
            athleteRepository.getAthleteById(athleteId)
                .catch { /* ignore */ }
                .collect { _athlete.value = it }
        }

        // Load performances
        viewModelScope.launch {
            performanceRepository.getPerformancesForAthlete(athleteId)
                .catch { /* ignore */ }
                .collect {
                    _performances.value = it
                    _isLoading.value = false
                }
        }

        // Load badges
        viewModelScope.launch {
            badgeRepository.getBadgesForAthlete(athleteId)
                .catch { /* ignore */ }
                .collect { _badges.value = it }
        }
    }

    /**
     * Returns the athlete's best sprint time as a formatted string.
     */
    fun getBestSprint(): String {
        val best = _performances.value
            .filter { it.sprintTime > 0 }
            .minByOrNull { it.sprintTime }
        return if (best != null) "%.2fs".format(best.sprintTime) else "—"
    }

    /**
     * Returns the athlete's distinct activity types as a joined string.
     */
    fun getEventTypes(): String {
        val types = _performances.value
            .map { it.activityType }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
        return if (types.isNotEmpty()) types.joinToString(", ") else "—"
    }
}
