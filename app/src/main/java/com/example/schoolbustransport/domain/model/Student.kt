package com.example.schoolbustransport.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a student user of the transport system.
 * Grade is stored as String to match the database enum (PP1, PP2, Grade1-6).
 */
data class Student(
    @DocumentId val id: String = "",
    val name: String = "",
    val grade: String? = null,
    val school: String = "",
    val parentId: String = "",

    /**
     * Latitude of the student's pickup location. Nullable if not set.
     */
    val pickupLat: Double? = null,

    /**
     * Longitude of the student's pickup location. Nullable if not set.
     */
    val pickupLng: Double? = null
)
