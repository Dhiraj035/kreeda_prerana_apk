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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Data class for a single recent activity item displayed on the dashboard.
 */
data class RecentActivity(
    val athleteName: String,
    val activityType: String,
    val score: String,
    val dateMillis: Long
)

/**
 * ViewModel for the Home dashboard screen.
 * Provides overview counts (athletes, trials, badges) and recent activity.
 */
class DashboardViewModel : ViewModel() {

    private val athleteRepository = AthleteRepository()
    private val performanceRepository = PerformanceRepository()
    private val badgeRepository = BadgeRepository()

    private val _athleteCount = MutableStateFlow(0)
    val athleteCount: StateFlow<Int> = _athleteCount.asStateFlow()

    private val _trialCount = MutableStateFlow(0)
    val trialCount: StateFlow<Int> = _trialCount.asStateFlow()

    private val _badgeCount = MutableStateFlow(0)
    val badgeCount: StateFlow<Int> = _badgeCount.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivities: StateFlow<List<RecentActivity>> = _recentActivities.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                athleteRepository.getAthletes(),
                performanceRepository.getAllPerformances(),
                badgeRepository.getAllBadges()
            ) { athletes, performances, badges ->
                Triple(athletes, performances, badges)
            }
            .catch { /* silently handle errors — counts stay at 0 */ }
            .collect { (athletes, performances, badges) ->
                _athleteCount.value = athletes.size
                _trialCount.value = performances.size
                _badgeCount.value = badges.size
                _recentActivities.value = buildRecentActivities(athletes, performances)
                _isLoading.value = false
            }
        }
    }

    /**
     * Builds the 5 most recent activity entries by joining performances with athlete names.
     */
    private fun buildRecentActivities(
        athletes: List<Athlete>,
        performances: List<Performance>
    ): List<RecentActivity> {
        val athleteMap = athletes.associateBy { it.athleteId }
        return performances
            .sortedByDescending { it.date }
            .take(5)
            .map { perf ->
                val name = athleteMap[perf.athleteId]?.name ?: "Unknown"
                val score = when {
                    perf.sprintTime > 0 -> "%.2f sec".format(perf.sprintTime)
                    perf.distance > 0 -> "%.1f m".format(perf.distance)
                    else -> "—"
                }
                RecentActivity(
                    athleteName = name,
                    activityType = perf.activityType.ifBlank { "Trial" },
                    score = score,
                    dateMillis = perf.date
                )
            }
    }
}
