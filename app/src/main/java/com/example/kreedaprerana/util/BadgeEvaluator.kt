package com.example.kreedaprerana.util

import com.example.kreedaprerana.data.model.Badge
import com.example.kreedaprerana.data.model.Performance

/**
 * Evaluates an athlete's performances against sport-specific benchmarks
 * and returns any badges they have earned.
 *
 * Badge Levels:
 * - "District Level Ready"  — meets district-level qualifying standards
 * - "State Level Potential" — meets state-level qualifying standards
 * - "Rising Star"           — ≥3 trials logged with improvement
 * - "National Hope"         — exceptional performance (state-level in 2+ events)
 *
 * Benchmarks (default values):
 * - Sprint 100m:  District ≤ 13.0s,  State ≤ 11.5s
 * - Sprint 200m:  District ≤ 26.0s,  State ≤ 23.5s
 * - Swimming:     District ≤ 70.0s,  State ≤ 60.0s
 * - Distance:     District ≥ 30m,    State ≥ 45m
 */
object BadgeEvaluator {

    // Sprint benchmarks: activityType → (districtThreshold, stateThreshold) in seconds
    // Lower is better for sprints
    private val sprintBenchmarks = mapOf(
        "Sprint" to Pair(13.0, 11.5),
        "100m Sprint" to Pair(13.0, 11.5),
        "200m Sprint" to Pair(26.0, 23.5),
        "Swimming" to Pair(70.0, 60.0)
    )

    // Distance benchmarks: activityType → (districtThreshold, stateThreshold) in meters
    // Higher is better for distance events
    private val distanceBenchmarks = mapOf(
        "Distance Throw" to Pair(30.0, 45.0),
        "Long Jump" to Pair(5.0, 6.5),
        "Shot Put" to Pair(10.0, 13.0)
    )

    /**
     * Evaluates all performances and returns a list of earned badges.
     *
     * @param athleteId The athlete's Firestore document ID
     * @param performances All recorded performances for this athlete
     * @param existingBadges Already-earned badges (to prevent duplicates)
     * @return List of newly earned Badge objects
     */
    fun evaluateBadges(
        athleteId: String,
        performances: List<Performance>,
        existingBadges: List<Badge> = emptyList()
    ): List<Badge> {
        val newBadges = mutableListOf<Badge>()
        val existingBadgeNames = existingBadges.map { it.badgeName }.toSet()
        var stateLevelCount = 0

        // Check sprint-based events (lower time is better)
        for ((activityType, thresholds) in sprintBenchmarks) {
            val bestTime = performances
                .filter { it.activityType == activityType && it.sprintTime > 0 }
                .minByOrNull { it.sprintTime }
                ?.sprintTime ?: continue

            val (districtThreshold, stateThreshold) = thresholds

            // State level is higher achievement — check first
            if (bestTime <= stateThreshold) {
                stateLevelCount++
                val badgeName = "State Level Potential - $activityType"
                if (badgeName !in existingBadgeNames) {
                    newBadges.add(
                        Badge(
                            athleteId = athleteId,
                            badgeName = badgeName,
                            level = "State",
                            earnedDate = System.currentTimeMillis()
                        )
                    )
                }
            } else if (bestTime <= districtThreshold) {
                val badgeName = "District Level Ready - $activityType"
                if (badgeName !in existingBadgeNames) {
                    newBadges.add(
                        Badge(
                            athleteId = athleteId,
                            badgeName = badgeName,
                            level = "District",
                            earnedDate = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        // Check distance-based events (higher distance is better)
        for ((activityType, thresholds) in distanceBenchmarks) {
            val bestDistance = performances
                .filter { it.activityType == activityType && it.distance > 0 }
                .maxByOrNull { it.distance }
                ?.distance ?: continue

            val (districtThreshold, stateThreshold) = thresholds

            if (bestDistance >= stateThreshold) {
                stateLevelCount++
                val badgeName = "State Level Potential - $activityType"
                if (badgeName !in existingBadgeNames) {
                    newBadges.add(
                        Badge(
                            athleteId = athleteId,
                            badgeName = badgeName,
                            level = "State",
                            earnedDate = System.currentTimeMillis()
                        )
                    )
                }
            } else if (bestDistance >= districtThreshold) {
                val badgeName = "District Level Ready - $activityType"
                if (badgeName !in existingBadgeNames) {
                    newBadges.add(
                        Badge(
                            athleteId = athleteId,
                            badgeName = badgeName,
                            level = "District",
                            earnedDate = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        // ── Rising Star: ≥3 trials with measurable improvement ──
        val risingStarName = "Rising Star"
        if (risingStarName !in existingBadgeNames && performances.size >= 3) {
            val sprintPerfs = performances.filter { it.sprintTime > 0 }.sortedBy { it.date }
            val hasImprovement = if (sprintPerfs.size >= 2) {
                sprintPerfs.last().sprintTime < sprintPerfs.first().sprintTime
            } else {
                true // 3+ trials is commitment enough
            }
            if (hasImprovement) {
                newBadges.add(
                    Badge(
                        athleteId = athleteId,
                        badgeName = risingStarName,
                        level = "Rising Star",
                        earnedDate = System.currentTimeMillis()
                    )
                )
            }
        }

        // ── National Hope: State-level performance in 2+ different events ──
        val nationalHopeName = "National Hope"
        val existingStateBadges = existingBadges.count { it.level == "State" }
        val totalStateLevel = stateLevelCount + existingStateBadges
        if (nationalHopeName !in existingBadgeNames && totalStateLevel >= 2) {
            newBadges.add(
                Badge(
                    athleteId = athleteId,
                    badgeName = nationalHopeName,
                    level = "National",
                    earnedDate = System.currentTimeMillis()
                )
            )
        }

        return newBadges
    }
}
