package com.example.schoolbustransport.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Represents a single bus trip.
 * This is a core domain model matching Firestore structure.
 */
data class Trip(
    @DocumentId val id: String = "",
    val tripName: String = "",
    val routeId: String = "", // Reference to routes collection
    val busId: String = "", // Reference to buses collection
    val driverId: String = "", // Reference to users collection (role: DRIVER)
    val grade: String = "", // Student class for this trip
    val status: TripStatus = TripStatus.SCHEDULED,
    val scheduledDate: Timestamp? = null, // When trip is scheduled
    val departureTime: String? = null, // Time string like "08:00"
    val startTime: Timestamp? = null, // When trip actually started
    val endTime: Timestamp? = null, // When trip actually ended
    val studentIds: List<String> = emptyList(), // Array of student IDs
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    // Populated fields (not stored in Firestore, loaded separately)
    val bus: Bus = Bus(),
    val route: Route = Route(),
    val driver: User = User(),
    val students: List<Student> = emptyList()
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
