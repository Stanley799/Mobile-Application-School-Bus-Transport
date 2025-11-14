package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

data class AttendanceDto(
    val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("student_id") val studentId: Int,
    val status: String?,
    val timestamp: String,
    @SerializedName("marked_by") val markedBy: Int?
)

data class AttendanceRequest(
    @SerializedName("studentId") val studentId: Int,
    val status: String // "PRESENT" or "ABSENT"
)

