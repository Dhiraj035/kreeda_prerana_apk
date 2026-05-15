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
 * Summary statistics for the analytics dashboard.
 */
data class AnalyticsStats(
    val avgImprovement: String = "—",
    val topScore: String = "—",
    val topScoreLabel: String = "",
    val athleteCount: Int = 0,
    val trialCount: Int = 0
)

/**
 * ViewModel for the Analytics screen.
 * Prepares performance history data for chart rendering and computes summary statistics.
 */
class AnalyticsViewModel : ViewModel() {

    private val athleteRepository = AthleteRepository()
    private val performanceRepository = PerformanceRepository()

    private val _performances = MutableStateFlow<List<Performance>>(emptyList())
    /** All performances for chart data — sorted by date ascending for plotting. */
    val performances: StateFlow<List<Performance>> = _performances.asStateFlow()

    private val _stats = MutableStateFlow(AnalyticsStats())
    /** Computed summary statistics. */
    val stats: StateFlow<AnalyticsStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    /** True while initial data is loading. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error message if data loading fails. */
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Normalized data points for the chart (values between 0.0 and 1.0).
     * Each point represents a performance's sprint time normalized against the range.
     */
    private val _chartPoints = MutableStateFlow<List<Float>>(emptyList())
    val chartPoints: StateFlow<List<Float>> = _chartPoints.asStateFlow()

    /** Secondary chart line for distance-based performances. */
    private val _chartPointsSecondary = MutableStateFlow<List<Float>>(emptyList())
    val chartPointsSecondary: StateFlow<List<Float>> = _chartPointsSecondary.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                athleteRepository.getAthletes(),
                performanceRepository.getAllPerformances()
            ) { athletes, performances ->
                Pair(athletes, performances)
            }
            .catch { e ->
                _error.value = e.message ?: "Failed to load analytics"
                _isLoading.value = false
            }
            .collect { (athletes, performances) ->
                _performances.value = performances
                computeStats(athletes, performances)
                computeChartPoints(performances)
                _isLoading.value = false
            }
        }
    }

    /**
     * Computes summary statistics from athlete and performance data.
     */
    private fun computeStats(athletes: List<Athlete>, performances: List<Performance>) {
        val athleteCount = athletes.size
        val trialCount = performances.size

        // Find the best (lowest) sprint time across all performances
        val bestSprint = performances
            .filter { it.sprintTime > 0 }
            .minByOrNull { it.sprintTime }

        val topScore = if (bestSprint != null) {
            "%.2fs".format(bestSprint.sprintTime)
        } else {
            "—"
        }

        val topScoreLabel = if (bestSprint != null) {
            bestSprint.activityType.ifBlank { "Sprint" }
        } else {
            ""
        }

        // Calculate average improvement across all athletes
        val avgImprovement = calculateAverageImprovement(performances)

        _stats.value = AnalyticsStats(
            avgImprovement = avgImprovement,
            topScore = topScore,
            topScoreLabel = topScoreLabel,
            athleteCount = athleteCount,
            trialCount = trialCount
        )
    }

    /**
     * Calculates the average improvement percentage across all athletes.
     * Compares each athlete's latest performance to their first performance.
     */
    private fun calculateAverageImprovement(performances: List<Performance>): String {
        val grouped = performances
            .filter { it.sprintTime > 0 }
            .groupBy { it.athleteId }

        val improvements = grouped.mapNotNull { (_, perfs) ->
            if (perfs.size < 2) return@mapNotNull null
            val sorted = perfs.sortedBy { it.date }
            val first = sorted.first().sprintTime
            val last = sorted.last().sprintTime
            if (first > 0) ((first - last) / first) * 100 else null
        }

        return if (improvements.isNotEmpty()) {
            val avg = improvements.average()
            "%+.0f%%".format(avg)
        } else {
            "—"
        }
    }

    /**
     * Computes normalized chart data points from performance history.
     * Sprint times are inverted (lower time = higher chart position = better).
     * Distance values are normalized directly (higher = better).
     */
    private fun computeChartPoints(performances: List<Performance>) {
        // Sprint-based performances (primary line)
        val sprintPerfs = performances
            .filter { it.sprintTime > 0 }
            .sortedBy { it.date }

        if (sprintPerfs.isNotEmpty()) {
            val maxTime = sprintPerfs.maxOf { it.sprintTime }
            val minTime = sprintPerfs.minOf { it.sprintTime }
            val range = maxTime - minTime

            _chartPoints.value = if (range > 0) {
                // Invert: lower time → higher value on chart
                sprintPerfs.map { ((maxTime - it.sprintTime) / range).toFloat() }
            } else {
                sprintPerfs.map { 0.5f } // All same value
            }
        } else {
            _chartPoints.value = emptyList()
        }

        // Distance-based performances (secondary line)
        val distPerfs = performances
            .filter { it.distance > 0 }
            .sortedBy { it.date }

        if (distPerfs.isNotEmpty()) {
            val maxDist = distPerfs.maxOf { it.distance }
            val minDist = distPerfs.minOf { it.distance }
            val range = maxDist - minDist

            _chartPointsSecondary.value = if (range > 0) {
                distPerfs.map { ((it.distance - minDist) / range).toFloat() }
            } else {
                distPerfs.map { 0.5f }
            }
        } else {
            _chartPointsSecondary.value = emptyList()
        }
    }
}
