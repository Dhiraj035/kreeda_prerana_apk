package com.example.kreedaprerana.data.model

/**
 * Represents a registered Coach/Teacher stored in the Firestore "coaches" collection.
 */
data class Coach(
    val uid: String = "",
    val fullName: String = "",
    val schoolName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
