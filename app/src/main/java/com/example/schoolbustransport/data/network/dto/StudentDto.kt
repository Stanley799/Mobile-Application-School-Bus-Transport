package com.example.schoolbustransport.data.network.dto

data class StudentDto(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val admission: String?,
    val grade: String?,
    val stream: String?,
    val parentId: String?
)
