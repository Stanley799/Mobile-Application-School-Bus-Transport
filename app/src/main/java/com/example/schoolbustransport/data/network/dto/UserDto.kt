data class UpdateUserProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?
)
import com.google.gson.annotations.SerializedName
/**
 * DTO for registration request, matching backend fields.
 */
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)
package com.example.schoolbustransport.data.network.dto

import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.domain.model.UserRole

/**
 * DTO for user data.
 */
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val phone: String?,
    val role: String,
    val image: String? = null
)

/**
 * Mapper function to convert UserDto to User domain model.
 */
fun UserDto.toUser(): User {
    return User(
        id = id.toString(),
        email = email,
        name = name,
        phone = phone,
        role = UserRole.fromString(role),
        image = image
    )
}
