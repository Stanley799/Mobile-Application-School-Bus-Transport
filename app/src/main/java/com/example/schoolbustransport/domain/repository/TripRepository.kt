package com.example.schoolbustransport.domain.repository

import com.example.schoolbustransport.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for trip-related data.
 */
interface TripRepository {
    fun getTrips(date: String? = null): Flow<List<Trip>>
    fun getTripDetails(tripId: String): Flow<Trip>
    suspend fun startTrip(tripId: String): Result<Trip>
    suspend fun endTrip(tripId: String): Result<Trip>
    suspend fun markAttendance(tripId: String, studentId: String, status: String): Result<Unit>

    // --- Trip Feedback ---
    suspend fun submitTripFeedback(tripId: String, rating: Int, comment: String?, studentId: Int? = null): Result<Unit>
    fun getTripFeedback(tripId: String): Flow<List<com.example.schoolbustransport.domain.model.TripFeedback>>
}
