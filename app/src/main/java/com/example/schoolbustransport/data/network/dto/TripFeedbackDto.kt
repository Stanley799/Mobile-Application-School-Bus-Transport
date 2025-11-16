package com.example.schoolbustransport.data.network.dto

data class TripFeedbackDto(
    val id: String,
    val tripId: String?,
    val parentId: String?,
    val studentId: String?,
    val rating: Float?,
    val comment: String?,
    val createdAt: String?,
    val parent: ParentLiteDto?,
    val student: StudentLiteDto?
)

data class ParentLiteDto(
    val firstName: String?,
    val lastName: String?
)

data class StudentLiteDto(
    val firstName: String?,
    val lastName: String?
)
