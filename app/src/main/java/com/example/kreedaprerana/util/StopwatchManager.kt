package com.example.kreedaprerana.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Accurate stopwatch implementation using System.nanoTime() for high precision.
 * Updates every 10ms and provides elapsed time in milliseconds.
 *
 * Usage:
 *   val stopwatch = StopwatchManager(viewModelScope)
 *   stopwatch.start()
 *   // Observe stopwatch.elapsedMillis for live updates
 *   stopwatch.stop()
 *   stopwatch.reset()
 */
class StopwatchManager(private val scope: CoroutineScope) {

    private val _elapsedMillis = MutableStateFlow(0L)
    /** Elapsed time in milliseconds, updated every ~10ms while running. */
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    /** Whether the stopwatch is currently running. */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var startTimeNano = 0L
    private var accumulatedNano = 0L
    private var timerJob: Job? = null

    /** Starts or resumes the stopwatch. */
    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTimeNano = System.nanoTime()
        timerJob = scope.launch {
            while (_isRunning.value) {
                val now = System.nanoTime()
                val totalNano = accumulatedNano + (now - startTimeNano)
                _elapsedMillis.value = totalNano / 1_000_000
                delay(10L) // Update every 10ms for smooth display
            }
        }
    }

    /** Stops the stopwatch, preserving elapsed time for potential resume. */
    fun stop() {
        if (!_isRunning.value) return
        _isRunning.value = false
        accumulatedNano += System.nanoTime() - startTimeNano
        timerJob?.cancel()
        timerJob = null
        // Final accurate update
        _elapsedMillis.value = accumulatedNano / 1_000_000
    }

    /** Resets the stopwatch to zero. */
    fun reset() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        accumulatedNano = 0L
        startTimeNano = 0L
        _elapsedMillis.value = 0L
    }

    companion object {
        /**
         * Formats milliseconds into "MM:SS.mm" string with 2-decimal-place precision.
         * Example: 12340L → "00:12.34"
         */
        fun formatTime(millis: Long): String {
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val centiseconds = (millis % 1000) / 10
            return "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
        }

        /**
         * Converts milliseconds to seconds with 2-decimal-place precision.
         * Example: 12340L → 12.34
         */
        fun millisToSeconds(millis: Long): Double {
            return (millis / 10).toDouble() / 100.0
        }
    }
}
