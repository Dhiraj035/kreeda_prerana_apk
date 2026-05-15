package com.example.kreedaprerana.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreedaprerana.data.model.Athlete
import com.example.kreedaprerana.data.model.Badge
import com.example.kreedaprerana.data.model.Performance
import com.example.kreedaprerana.data.repository.AthleteRepository
import com.example.kreedaprerana.data.repository.BadgeRepository
import com.example.kreedaprerana.data.repository.PerformanceRepository
import com.example.kreedaprerana.util.BadgeEvaluator
import com.example.kreedaprerana.util.StopwatchManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Trial Logger screen.
 * Manages the stopwatch, athlete selection, and performance logging.
 * After each logged trial, it evaluates and saves any newly earned badges.
 */
class TrialLoggerViewModel : ViewModel() {

    private val athleteRepository = AthleteRepository()
    private val performanceRepository = PerformanceRepository()
    private val badgeRepository = BadgeRepository()

    /** The stopwatch instance — observe elapsedMillis and isRunning from it. */
    val stopwatch = StopwatchManager(viewModelScope)

    private val _athletes = MutableStateFlow<List<Athlete>>(emptyList())
    /** List of all athletes for the selection dropdown. */
    val athletes: StateFlow<List<Athlete>> = _athletes.asStateFlow()

    private val _isLogging = MutableStateFlow(false)
    /** True while a trial is being saved to Firestore. */
    val isLogging: StateFlow<Boolean> = _isLogging.asStateFlow()

    private val _logSuccess = MutableStateFlow(false)
    /** Becomes true briefly after a successful trial log. */
    val logSuccess: StateFlow<Boolean> = _logSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    /** Error message from the latest failed operation. */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _newBadges = MutableStateFlow<List<Badge>>(emptyList())
    /** Any new badges earned from the latest logged trial. */
    val newBadges: StateFlow<List<Badge>> = _newBadges.asStateFlow()

    init {
        // Load athlete list for the dropdown
        viewModelScope.launch {
            athleteRepository.getAthletes()
                .catch { e ->
                    _error.value = e.message ?: "Failed to load athletes"
                }
                .collect { athleteList ->
                    _athletes.value = athleteList
                }
        }
    }

    /**
     * Logs a trial performance to Firestore and evaluates badges.
     *
     * @param athleteId Firestore ID of the selected athlete
     * @param sprintTimeMillis Stopwatch time in milliseconds
     * @param distance Distance covered in meters (0 if not applicable)
     * @param activityType Type of activity (e.g., "Sprint", "Distance Throw")
     */
    fun logTrial(
        athleteId: String,
        sprintTimeMillis: Long,
        distance: Double,
        activityType: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLogging.value = true
            _error.value = null
            _logSuccess.value = false
            _newBadges.value = emptyList()

            val sprintTimeSeconds = StopwatchManager.millisToSeconds(sprintTimeMillis)

            val performance = Performance(
                athleteId = athleteId,
                sprintTime = sprintTimeSeconds,
                distance = distance,
                activityType = activityType,
                notes = notes,
                date = System.currentTimeMillis()
            )

            val saveResult = performanceRepository.savePerformance(performance)

            saveResult.onSuccess {
                _logSuccess.value = true

                // Evaluate badges after saving the performance
                evaluateAndSaveBadges(athleteId)
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to log trial"
            }

            _isLogging.value = false
        }
    }

    /**
     * Fetches all performances and existing badges for the athlete,
     * evaluates new badge eligibility, and saves any earned badges.
     */
    private suspend fun evaluateAndSaveBadges(athleteId: String) {
        try {
            // We need a one-shot fetch of performances and badges for evaluation
            val performancesFlow = performanceRepository.getPerformancesForAthlete(athleteId)
            val badgesFlow = badgeRepository.getBadgesForAthlete(athleteId)

            // Collect first emission from each flow
            var performances = emptyList<Performance>()
            var existingBadges = emptyList<Badge>()

            // Use a simple collect with a limit
            val perfJob = viewModelScope.launch {
                performancesFlow.collect {
                    performances = it
                    return@collect // Take first emission only
                }
            }
            // Small delay to let the snapshot fire
            kotlinx.coroutines.delay(500)
            perfJob.cancel()

            val badgeJob = viewModelScope.launch {
                badgesFlow.collect {
                    existingBadges = it
                    return@collect
                }
            }
            kotlinx.coroutines.delay(500)
            badgeJob.cancel()

            val newBadgeList = BadgeEvaluator.evaluateBadges(
                athleteId = athleteId,
                performances = performances,
                existingBadges = existingBadges
            )

            // Save each new badge to Firestore
            for (badge in newBadgeList) {
                badgeRepository.saveBadge(badge)
            }

            _newBadges.value = newBadgeList
        } catch (e: Exception) {
            // Badge evaluation failure is non-critical, don't override trial success
            e.printStackTrace()
        }
    }

    /** Resets the logSuccess flag after the UI has consumed it. */
    fun resetLogSuccess() {
        _logSuccess.value = false
    }

    /** Clears the current error message. */
    fun clearError() {
        _error.value = null
    }

    /** Clears the new badges notification. */
    fun clearNewBadges() {
        _newBadges.value = emptyList()
    }
}
