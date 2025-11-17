package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.model.TripStatus
import com.example.schoolbustransport.domain.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth
) : TripRepository {
    
    private val mapper = TripDataMapper(firestore)

    override fun getTrips(): Flow<List<Trip>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        // Get user role for filtering
        val userRole = kotlinx.coroutines.runBlocking {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                userDoc.data?.get("role") as? String ?: "PARENT"
            } catch (e: Exception) {
                "PARENT"
            }
        }
        
        val query = when (userRole) {
            "ADMIN" -> firestore.collection("trips")
            "DRIVER" -> firestore.collection("trips").whereEqualTo("driverId", userId)
            else -> {
                // For parents, we'll query all trips and filter client-side
                firestore.collection("trips")
            }
        }
        
        val listenerRegistration = query
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Use coroutine scope to populate trips
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Populate trips with related data (populateTrip is suspend)
                            val trips = snapshot.documents.mapNotNull { doc ->
                                try {
                                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                                        mapper.populateTrip(doc)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            
                            // Client-side filter for parents
                            val filteredTrips = if (userRole == "PARENT" && trips.isNotEmpty()) {
                                try {
                                    val studentsSnapshot = firestore.collection("students")
                                        .whereEqualTo("parentId", userId)
                                        .get()
                                        .await()
                                    val studentIds = studentsSnapshot.documents.map { it.id }.toSet()
                                    trips.filter { trip ->
                                        trip.students.any { it.id in studentIds } ||
                                        trip.studentIds.any { it in studentIds }
                                    }
                                } catch (e: Exception) {
                                    trips
                                }
                            } else {
                                trips
                            }
                            
                            trySend(filteredTrips)
                        } catch (e: Exception) {
                            // Send empty on error
                            trySend(emptyList())
                        }
                    }
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
                if (snapshot != null && snapshot.exists()) {
                    // Use coroutine scope to populate trip
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val trip = kotlinx.coroutines.withContext(Dispatchers.IO) {
                                mapper.populateTrip(snapshot)
                            }
                            trySend(trip)
                        } catch (e: Exception) {
                            trySend(null)
                        }
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun startTrip(tripId: String): Result<Unit> {
        return try {
            val data = hashMapOf("tripId" to tripId)
            functions.getHttpsCallable("startTrip")
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun endTrip(tripId: String): Result<Unit> {
        return try {
            val data = hashMapOf("tripId" to tripId)
            functions.getHttpsCallable("endTrip")
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAttendance(tripId: String, studentId: String, status: String): Result<Unit> {
        return try {
            val isPresent = status == "present" || status == "Present" || status == "PRESENT"
            val data = hashMapOf(
                "tripId" to tripId,
                "studentId" to studentId,
                "isPresent" to isPresent
            )
            functions.getHttpsCallable("markAttendance")
                .call(data)
                .await()
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
