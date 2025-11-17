package com.example.schoolbustransport.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a student user of the transport system.
 * Grade is stored as String (Grade5, Grade6, Grade7, Grade8, Grade9).
 */
data class Student(
    @DocumentId val id: String = "",
    val name: String = "",
    val age: Int? = null,
    val gender: String? = null, // "Male", "Female", "Other"
    val grade: String? = null, // "Grade5", "Grade6", "Grade7", "Grade8", "Grade9"
    val school: String = "",
    val parentId: String = "",
    val homeLocation: String = "", // Home address/location

    /**
     * Latitude of the student's pickup location. Nullable if not set.
     */
    val pickupLat: Double? = null,

    /**
     * Longitude of the student's pickup location. Nullable if not set.
     */
    val pickupLng: Double? = null
)
