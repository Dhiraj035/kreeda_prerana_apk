package com.example.kreedaprerana.data.model

import com.google.firebase.firestore.Exclude

/**
 * Represents an athlete profile stored in Firestore "athletes" collection.
 * All fields have defaults so Firestore can deserialize documents automatically.
 */
data class Athlete(
    val athleteId: String = "",
    val name: String = "",
    val age: Int = 0,
    val sport: String = "",
    val gender: String = "",
    val schoolName: String = "",
    val profileImage: String = "",
    val coachId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Computes initials from the athlete's name (e.g., "Arjun Mehta" → "AM"). */
    @get:Exclude
    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
}
