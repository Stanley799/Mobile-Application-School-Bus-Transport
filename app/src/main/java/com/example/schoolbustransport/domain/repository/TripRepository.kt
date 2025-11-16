package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for trip-related data.
 */
interface TripRepository {
    fun getTrips(): Flow<List<Trip>>
    fun getTripDetails(tripId: String): Flow<Trip?>
    suspend fun startTrip(tripId: String): Result<Unit>
    suspend fun endTrip(tripId: String): Result<Unit>
    suspend fun markAttendance(tripId: String, studentId: String, status: String): Result<Unit>
    fun getTripFeedback(tripId: String): kotlinx.coroutines.flow.Flow<List<com.example.schoolbustransport.domain.model.TripFeedback>>
    suspend fun submitTripFeedback(tripId: String, rating: Int, comment: String?, studentId: String? = null): Result<Unit>
}
