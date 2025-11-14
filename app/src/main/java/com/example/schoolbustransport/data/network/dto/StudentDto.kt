package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

data class StudentDto(
    val id: Int,
    @SerializedName("student_fname") val firstName: String,
    @SerializedName("student_lname") val lastName: String,
    val admission: Int,
    val grade: String?,
    val stream: String?,
    @SerializedName("parent_id") val parentId: Int?,
    val parent: ParentInfo?,
    @SerializedName("pickup_latitude") val pickupLat: Double? = null,
    @SerializedName("pickup_longitude") val pickupLng: Double? = null
)

data class ParentInfo(
    val id: Int,
    @SerializedName("parent_fname") val firstName: String,
    @SerializedName("parent_lname") val lastName: String,
    val address: String?,
    val user: UserInfo?
)

data class CreateStudentRequest(
    @SerializedName("studentFname") val firstName: String,
    @SerializedName("studentLname") val lastName: String,
    val admission: Int,
    val grade: String? = null,
    val stream: String? = null,
    @SerializedName("parentId") val parentId: Int? = null,
    @SerializedName("pickupLatitude") val pickupLat: Double? = null,
    @SerializedName("pickupLongitude") val pickupLng: Double? = null
)

data class UpdateStudentRequest(
    @SerializedName("studentFname") val firstName: String? = null,
    @SerializedName("studentLname") val lastName: String? = null,
    val grade: String? = null,
    val stream: String? = null,
    @SerializedName("pickupLatitude") val pickupLat: Double? = null,
    @SerializedName("pickupLongitude") val pickupLng: Double? = null
)
