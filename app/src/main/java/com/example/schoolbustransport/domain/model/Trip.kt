package com.example.schoolbustransport.domain.model

import java.util.Date

/**
 * Represents a single bus trip.
 * This is a core domain model.
 */
data class Trip(
    val id: String,
    val startTime: Date,
    val endTime: Date?,
    val status: TripStatus,
    val bus: Bus,
    val route: Route,
    val driver: User,
    val students: List<Student>
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
