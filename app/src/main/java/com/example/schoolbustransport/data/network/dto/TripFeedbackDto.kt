package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for submitting parent feedback after a trip.
 */
data class TripFeedbackRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("studentId") val studentId: Int? = null
)

/**
 * Data Transfer Object for feedback response from backend.
 * Mapping to domain model is handled by TripFeedbackMapper.kt
 */
data class TripFeedbackDto(
    @SerializedName("id") val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("parent_id") val parentId: Int,
    @SerializedName("student_id") val studentId: Int?,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("parent") val parent: ParentNameDto?,
    @SerializedName("student") val student: StudentNameDto?
)

data class ParentNameDto(
    @SerializedName("parent_fname") val firstName: String?,
    @SerializedName("parent_lname") val lastName: String?
)

data class StudentNameDto(
    @SerializedName("student_fname") val firstName: String?,
    @SerializedName("student_lname") val lastName: String?
)
