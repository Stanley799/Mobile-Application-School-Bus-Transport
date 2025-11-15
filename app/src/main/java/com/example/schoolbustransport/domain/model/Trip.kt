package com.example.schoolbustransport.domain.model

import java.util.Date

/**
 * Represents a single bus trip.
 * This is a core domain model.
 */
data class Trip(
    val id: String = "",
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val status: TripStatus = TripStatus.SCHEDULED,
    val bus: Bus = Bus(),
    val route: Route = Route(),
    val driver: User = User(),
    val students: List<Student> = emptyList(),
    val departureTime: String? = null,
    val arrivalTime: String? = null
)

/**
 * Enum representing the status of a trip.
 * This ensures type safety for trip states.
 */
enum class TripStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
