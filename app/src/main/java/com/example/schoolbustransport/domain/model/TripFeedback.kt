package com.example.schoolbustransport.domain.model

import java.util.Date

/**
 * Domain model for parent feedback after a trip.
 */
data class TripFeedback(
    val id: Int,
    val tripId: Int,
    val parentId: Int,
    val studentId: Int?,
    val rating: Int,
    val comment: String?,
    val createdAt: Date?,
    val parentName: String?,
    val studentName: String?
)
