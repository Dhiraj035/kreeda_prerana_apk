package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Athlete
import com.example.kreedaprerana.data.model.Performance
import com.example.kreedaprerana.data.repository.AthleteRepository
import com.example.kreedaprerana.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Data class representing a single leaderboard entry,
 * combining athlete info with their best performance.
 */
data class LeaderboardEntry(
    val athleteId: String,
    val name: String,
    val sport: String,
    val bestScore: String,
    val bestValue: Double,
    val trend: String,
    val activityType: String
)

/**
 * A sport category with its ranked entries.
 */
data class SportLeaderboard(
    val activityType: String,
    val isTimeBased: Boolean,
    val entries: List<LeaderboardEntry>
)

/** Activities where lower time = better. */
private val timeBasedActivities = setOf(
    "100m Sprint", "200m Sprint", "400m Sprint", "Swimming"
)

/** Activities where higher distance/height = better. */
private val distanceBasedActivities = setOf(
    "Long Jump", "High Jump", "Shot Put", "Discus Throw",
    "Javelin Throw", "Triple Jump", "Pole Vault", "Distance Throw"
)

/**
 * ViewModel for the Leaderboard screen.
 * Produces sport-specific ranked leaderboards from real performance data.
 */
class LeaderboardViewModel : ViewModel() {

    private val athleteRepository = AthleteRepository()
    private val performanceRepository = PerformanceRepository()

    private val _sportLeaderboards = MutableStateFlow<List<SportLeaderboard>>(emptyList())
    /** Sport-categorized leaderboards. */
    val sportLeaderboards: StateFlow<List<SportLeaderboard>> = _sportLeaderboards.asStateFlow()

    /** Flat combined list for backward compat (all entries merged). */
    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    /** True while initial data is loading. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error message if data loading fails. */
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                athleteRepository.getAthletes(),
                performanceRepository.getAllPerformances()
            ) { athletes, performances ->
                buildSportLeaderboards(athletes, performances)
            }
            .catch { e ->
                _error.value = e.message ?: "Failed to load leaderboard"
                _isLoading.value = false
            }
            .collect { boards ->
                _sportLeaderboards.value = boards
                _leaderboardEntries.value = boards.flatMap { it.entries }
                _isLoading.value = false
            }
        }
    }

    /**
     * Builds per-sport leaderboards. Each sport gets its own ranked list
     * with the correct sorting direction.
     */
    private fun buildSportLeaderboards(
        athletes: List<Athlete>,
        performances: List<Performance>
    ): List<SportLeaderboard> {
        val athleteMap = athletes.associateBy { it.athleteId }

        // Group performances by activityType
        val perfsByActivity = performances.groupBy { it.activityType }

        return perfsByActivity.mapNotNull { (activityType, perfs) ->
            if (activityType.isBlank()) return@mapNotNull null

            val isTimeBased = activityType in timeBasedActivities

            // Group by athlete within this activity
            val perfsByAthlete = perfs.groupBy { it.athleteId }

            val entries = perfsByAthlete.mapNotNull { (athleteId, athletePerfs) ->
                val athlete = athleteMap[athleteId] ?: return@mapNotNull null

                if (isTimeBased) {
                    // Best = lowest sprint time
                    val best = athletePerfs.filter { it.sprintTime > 0 }.minByOrNull { it.sprintTime }
                        ?: return@mapNotNull null
                    val scoreStr = "%.2f s".format(best.sprintTime)
                    val trend = calculateTrend(athletePerfs, isTimeBased)
                    LeaderboardEntry(
                        athleteId = athleteId,
                        name = athlete.name,
                        sport = athlete.sport,
                        bestScore = scoreStr,
                        bestValue = best.sprintTime,
                        trend = trend,
                        activityType = activityType
                    )
                } else {
                    // Best = highest distance/height
                    val best = athletePerfs.filter { it.distance > 0 }.maxByOrNull { it.distance }
                        ?: return@mapNotNull null
                    val scoreStr = "%.2f m".format(best.distance)
                    val trend = calculateTrend(athletePerfs, isTimeBased)
                    LeaderboardEntry(
                        athleteId = athleteId,
                        name = athlete.name,
                        sport = athlete.sport,
                        bestScore = scoreStr,
                        bestValue = best.distance,
                        trend = trend,
                        activityType = activityType
                    )
                }
            }.let { list ->
                if (isTimeBased) {
                    list.sortedBy { it.bestValue } // Ascending for time
                } else {
                    list.sortedByDescending { it.bestValue } // Descending for distance
                }
            }

            if (entries.isEmpty()) return@mapNotNull null

            SportLeaderboard(
                activityType = activityType,
                isTimeBased = isTimeBased,
                entries = entries
            )
        }.sortedBy { it.activityType } // Alphabetical sport order
    }

    /**
     * Calculates improvement trend between the two most recent performances.
     */
    private fun calculateTrend(
        performances: List<Performance>,
        isTimeBased: Boolean
    ): String {
        val sorted = performances.sortedByDescending { it.date }
        if (sorted.size < 2) return "—"

        val latest = sorted[0]
        val previous = sorted[1]

        return if (isTimeBased && latest.sprintTime > 0 && previous.sprintTime > 0) {
            val diff = previous.sprintTime - latest.sprintTime
            when {
                diff > 0 -> "↑ %.2fs".format(diff)
                diff < 0 -> "↓ %.2fs".format(-diff)
                else -> "→"
            }
        } else if (!isTimeBased && latest.distance > 0 && previous.distance > 0) {
            val diff = latest.distance - previous.distance
            when {
                diff > 0 -> "↑ %.1fm".format(diff)
                diff < 0 -> "↓ %.1fm".format(-diff)
                else -> "→"
            }
        } else {
            "—"
        }
    }
}
