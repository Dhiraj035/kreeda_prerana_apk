package com.example.kreedaprerana.data.repository

import com.example.kreedaprerana.data.model.Coach
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles Firebase Authentication and Firestore operations for Coaches.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val coachesCollection = db.collection("coaches")

    /**
     * Checks if a user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Retrieves the current authenticated user's UID.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Fetches the current logged-in Coach's profile from Firestore.
     */
    suspend fun getCurrentCoach(): Result<Coach?> {
        val uid = getCurrentUserId() ?: return Result.failure(Exception("No user logged in"))
        return try {
            val document = coachesCollection.document(uid).get().await()
            if (document.exists()) {
                val coach = document.toObject(Coach::class.java)
                Result.success(coach)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registers a new Coach using Email/Password and saves their profile to Firestore.
     */
    suspend fun registerCoach(coach: Coach, password: String): Result<Coach> {
        return try {
            // 1. Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(coach.email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Failed to get user ID after signup")

            // 2. Save coach details to Firestore with the generated UID
            val newCoach = coach.copy(uid = uid)
            coachesCollection.document(uid).set(newCoach).await()

            Result.success(newCoach)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs in an existing Coach.
     */
    suspend fun loginCoach(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        auth.signOut()
    }
}
