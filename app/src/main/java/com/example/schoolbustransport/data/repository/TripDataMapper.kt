package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async

/**
 * Helper class to populate Trip objects with related data from Firestore
 */
class TripDataMapper(private val firestore: FirebaseFirestore) {
    
    suspend fun populateTrip(tripDoc: DocumentSnapshot): Trip {
        val tripData = tripDoc.data ?: emptyMap()
        val tripId = tripDoc.id
        
        // Load related data
        val routeId = tripData["routeId"] as? String ?: ""
        val busId = tripData["busId"] as? String ?: ""
        val driverId = tripData["driverId"] as? String ?: ""
        val studentIds = (tripData["studentIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        
        // Fetch route
        val route = if (routeId.isNotBlank()) {
            try {
                firestore.collection("routes").document(routeId).get().await()
                    .toObject(Route::class.java) ?: Route()
            } catch (e: Exception) {
                Route()
            }
        } else {
            Route()
        }
        
        // Fetch bus
        val bus = if (busId.isNotBlank()) {
            try {
                firestore.collection("buses").document(busId).get().await()
                    .toObject(Bus::class.java) ?: Bus()
            } catch (e: Exception) {
                Bus()
            }
        } else {
            Bus()
        }
        
        // Fetch driver
        val driver = if (driverId.isNotBlank()) {
            try {
                firestore.collection("users").document(driverId).get().await()
                    .toObject(User::class.java) ?: User()
            } catch (e: Exception) {
                User()
            }
        } else {
            User()
        }
        
        // Fetch students (batch if > 10) - use parallel coroutines for better performance
        val students = if (studentIds.isNotEmpty()) {
            try {
                coroutineScope {
                    if (studentIds.size <= 10) {
                        // Fetch in parallel
                        studentIds.map { studentId ->
                            async {
                                try {
                                    firestore.collection("students").document(studentId).get().await()
                                        .toObject(Student::class.java)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }.mapNotNull { it.await() }
                    } else {
                        // Batch fetch for > 10 students
                        studentIds.chunked(10).flatMap { chunk ->
                            chunk.map { studentId ->
                                async {
                                    try {
                                        firestore.collection("students").document(studentId).get().await()
                                            .toObject(Student::class.java)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }.mapNotNull { it.await() }
                        }
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        
        // Parse status
        val statusStr = tripData["status"] as? String ?: "SCHEDULED"
        val status = try {
            TripStatus.valueOf(statusStr)
        } catch (e: Exception) {
            TripStatus.SCHEDULED
        }
        
        return Trip(
            id = tripId,
            tripName = tripData["tripName"] as? String ?: "",
            routeId = routeId,
            busId = busId,
            driverId = driverId,
            grade = tripData["grade"] as? String ?: "",
            status = status,
            scheduledDate = tripData["scheduledDate"] as? com.google.firebase.Timestamp,
            departureTime = tripData["departureTime"] as? String,
            startTime = tripData["startTime"] as? com.google.firebase.Timestamp,
            endTime = tripData["endTime"] as? com.google.firebase.Timestamp,
            studentIds = studentIds,
            createdAt = tripData["createdAt"] as? com.google.firebase.Timestamp,
            updatedAt = tripData["updatedAt"] as? com.google.firebase.Timestamp,
            // Populated fields
            route = route,
            bus = bus,
            driver = driver,
            students = students
        )
    }
}

