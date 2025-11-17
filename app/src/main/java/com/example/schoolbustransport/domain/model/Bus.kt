package com.example.schoolbustransport.domain.model

/**
 * Represents a school bus in the system.
 */
data class Bus(
    val id: String = "",
    val name: String = "", // Bus name
    val licensePlate: String = "", // Number plate
    val numberOfSeats: Int = 0, // Number of seats
    val model: String? = null,
    val capacity: Int = 0 // Alias for numberOfSeats
)
