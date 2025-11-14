package com.example.schoolbustransport.data.repository

import com.example.schoolbustransport.data.network.ApiService
import com.example.schoolbustransport.data.network.dto.AttendanceRequest
import com.example.schoolbustransport.data.network.dto.toTrip
import com.example.schoolbustransport.domain.model.Trip
import com.example.schoolbustransport.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TripRepository {

    override fun getTrips(date: String?): Flow<List<Trip>> = flow {
        try {
            val response = apiService.getTrips(date = date, summary = false)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!.map { it.toTrip() })
            } else {
                // Improved error handling: Include the server's error message
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            // Re-throw with more context if possible
            throw Exception("Failed to fetch trips: ${e.message}", e)
        }
    }

    override fun getTripDetails(tripId: String): Flow<Trip> = flow {
        try {
            val response = apiService.getTripDetails(tripId)
            if (response.isSuccessful && response.body() != null) {
                emit(response.body()!!.toTrip())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch trip details: ${e.message}", e)
        }
    }

    override suspend fun startTrip(tripId: String): Result<Trip> {
        return try {
            val response = apiService.startTrip(tripId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toTrip())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to start trip: ${e.message}", e))
        }
    }

    override suspend fun endTrip(tripId: String): Result<Trip> {
        return try {
            val response = apiService.endTrip(tripId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toTrip())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to end trip: ${e.message}", e))
        }
    }

    override suspend fun markAttendance(tripId: String, studentId: String, status: String): Result<Unit> {
        return try {
            val response = apiService.markAttendance(
                tripId = tripId,
                request = AttendanceRequest(
                    studentId = studentId.toInt(),
                    status = status
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to mark attendance: ${e.message}", e))
        }
    }
}