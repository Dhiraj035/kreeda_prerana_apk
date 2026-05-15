package com.example.kreedaprerana.data.repository

import com.example.kreedaprerana.data.model.Athlete
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore CRUD operations for the "athletes" collection.
 */
class AthleteRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("athletes")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Adds a new athlete to Firestore.
     * @return Result containing the generated document ID on success.
     */
    suspend fun addAthlete(athlete: Athlete): Result<String> {
        return try {
            val docRef = collection.document()
            val athleteWithId = athlete.copy(athleteId = docRef.id, coachId = currentUserId)
            docRef.set(athleteWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a real-time Flow of all athletes, ordered by creation date (newest first).
     * Uses Firestore snapshot listeners so the UI updates automatically.
     */
    fun getAthletes(): Flow<List<Athlete>> = callbackFlow {
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
                        val athletes = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Athlete::class.java)
                        }?.sortedByDescending { it.createdAt } ?: emptyList()
                        trySend(athletes)
                    }
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            listener?.remove()
        }
    }

    /**
     * Returns a real-time Flow of a single athlete by ID.
     */
    fun getAthleteById(athleteId: String): Flow<Athlete?> = callbackFlow {
        val listener = collection.document(athleteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val athlete = snapshot?.toObject(Athlete::class.java)
                trySend(athlete)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Fetches a single athlete one-shot (not real-time).
     */
    suspend fun fetchAthleteById(athleteId: String): Result<Athlete?> {
        return try {
            val doc = collection.document(athleteId).get().await()
            Result.success(doc.toObject(Athlete::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing athlete document in Firestore.
     */
    suspend fun updateAthlete(athlete: Athlete): Result<Unit> {
        return try {
            collection.document(athlete.athleteId).set(athlete).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an athlete document by its ID.
     */
    suspend fun deleteAthlete(athleteId: String): Result<Unit> {
        return try {
            collection.document(athleteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
