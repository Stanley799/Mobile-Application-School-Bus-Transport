package com.example.schoolbustransport.domain.model

/**
 * Represents a school bus in the system.
 */
data class Bus(
    val id: String = "",
    val licensePlate: String = "",
    val model: String? = null,
    val capacity: Int = 0
)
