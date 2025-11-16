package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TripRepository {

    override fun getTrips(): Flow<List<Trip>> = callbackFlow {
        val listenerRegistration = firestore.collection("trips")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Trip::class.java))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getTripDetails(tripId: String): Flow<Trip?> = callbackFlow {
        val listenerRegistration = firestore.collection("trips").document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Trip::class.java))
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun startTrip(tripId: String): Result<Unit> {
        return try {
            firestore.collection("trips").document(tripId).update("status", TripStatus.IN_PROGRESS).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endTrip(tripId: String): Result<Unit> {
        return try {
            firestore.collection("trips").document(tripId).update("status", TripStatus.COMPLETED).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAttendance(tripId: String, studentId: String, status: String): Result<Unit> {
        return try {
            val field = "attendance.$studentId"
            firestore.collection("trips").document(tripId).update(field, status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override fun getTripFeedback(tripId: String): Flow<List<com.example.schoolbustransport.domain.model.TripFeedback>> = callbackFlow {
        // TODO: Implement actual Firestore feedback fetching
        trySend(emptyList())
        awaitClose {}
    }

    override suspend fun submitTripFeedback(tripId: String, rating: Int, comment: String?, studentId: String?): Result<Unit> {
        // TODO: Implement actual Firestore feedback submission
        return Result.success(Unit)
    }
}
