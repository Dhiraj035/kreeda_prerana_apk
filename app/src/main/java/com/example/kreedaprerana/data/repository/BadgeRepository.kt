package com.example.kreedaprerana.data.repository

import com.example.kreedaprerana.data.model.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore CRUD operations for the "badges" collection.
 */
class BadgeRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("badges")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Saves a badge to Firestore. Uses badgeId as the document ID to prevent duplicates.
     */
    suspend fun saveBadge(badge: Badge): Result<Unit> {
        return try {
            val docRef = if (badge.badgeId.isNotBlank()) {
                collection.document(badge.badgeId)
            } else {
                collection.document()
            }
            val badgeWithId = badge.copy(badgeId = docRef.id, coachId = currentUserId)
            docRef.set(badgeWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a real-time Flow of badges for a specific athlete.
     */
    fun getBadgesForAthlete(athleteId: String): Flow<List<Badge>> = callbackFlow {
        val listener = collection
            .whereEqualTo("athleteId", athleteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val badges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Badge::class.java)
                } ?: emptyList()
                trySend(badges)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Returns a real-time Flow of ALL badges across all athletes.
     */
    fun getAllBadges(): Flow<List<Badge>> = callbackFlow {
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
                        val badges = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Badge::class.java)
                        } ?: emptyList()
                        trySend(badges)
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
