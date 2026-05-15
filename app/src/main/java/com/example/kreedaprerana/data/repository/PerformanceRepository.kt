package com.example.kreedaprerana.data.repository

import com.example.kreedaprerana.data.model.Performance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore CRUD operations for the "performances" collection.
 *
 * Note: Compound queries (whereEqualTo + orderBy) require a Firestore composite index.
 * To avoid that requirement, we filter/sort in-memory instead.
 */
class PerformanceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("performances")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Saves a new performance record to Firestore.
     * @return Result containing the generated document ID on success.
     */
    suspend fun savePerformance(performance: Performance): Result<String> {
        return try {
            val docRef = collection.document()
            val perfWithId = performance.copy(performanceId = docRef.id, coachId = currentUserId)
            docRef.set(perfWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a real-time Flow of performances for a specific athlete.
     * Uses only whereEqualTo (no orderBy) to avoid needing a composite index.
     * Sorting is done in-memory after retrieval.
     */
    fun getPerformancesForAthlete(athleteId: String): Flow<List<Performance>> = callbackFlow {
        val listener = collection
            .whereEqualTo("athleteId", athleteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Don't close the flow — just send empty list and log
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val performances = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Performance::class.java)
                }?.sortedByDescending { it.date } ?: emptyList()
                trySend(performances)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Returns a real-time Flow of ALL performances across all athletes.
     * Used for leaderboard and analytics calculations.
     */
    fun getAllPerformances(): Flow<List<Performance>> = callbackFlow {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            listener?.remove()
            val uid = firebaseAuth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                listener = collection
                    .whereEqualTo("coachId", uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val performances = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Performance::class.java)
                        }?.sortedByDescending { it.date } ?: emptyList()
                        trySend(performances)
                    }
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            listener?.remove()
        }
    }
}
