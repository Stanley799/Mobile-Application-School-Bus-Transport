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
    val role: String
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
        role = UserRole.fromString(role)
    )
}
