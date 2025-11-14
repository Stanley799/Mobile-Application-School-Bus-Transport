package com.example.schoolbustransport.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for authentication responses.
 */
data class AuthResponse(
    val token: String,
    @SerializedName("userId") val userId: Int, // Server returns as number
    val role: String
) {
    // Helper property to get userId as String
    val userIdString: String get() = userId.toString()
}
