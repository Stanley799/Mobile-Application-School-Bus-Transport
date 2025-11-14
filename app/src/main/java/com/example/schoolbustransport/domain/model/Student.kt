package com.example.schoolbustransport.domain.model

/**
 * Represents a student user of the transport system.
 */
data class Student(
    val id: String,
    val name: String,
    val grade: Int,
    val school: String,
    val parentId: String,

    /**
     * Latitude of the student's pickup location. Nullable if not set.
     */
    val pickupLat: Double? = null,

    /**
     * Longitude of the student's pickup location. Nullable if not set.
     */
    val pickupLng: Double? = null
)
