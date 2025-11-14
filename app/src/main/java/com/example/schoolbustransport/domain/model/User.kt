package com.example.schoolbustransport.domain.model

/**
 * User domain model with sealed class for roles
 * Demonstrates Kotlin's sealed classes for type-safe role management
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val role: UserRole,
    val image: String? = null
)

/**
 * Sealed class for user roles - provides exhaustive when statements
 * Better than enum for complex role logic
 */
sealed class UserRole {
    object Admin : UserRole()
    object Driver : UserRole()
    object Parent : UserRole()

    fun toServerString(): String = when (this) {
        is Admin -> "ADMIN"
        is Driver -> "DRIVER"
        is Parent -> "PARENT"
    }

    companion object {
        fun fromString(role: String): UserRole = when (role.uppercase()) {
            "ADMIN" -> Admin
            "DRIVER" -> Driver
            "PARENT" -> Parent
            else -> throw IllegalArgumentException("Unknown role: $role")
        }
    }
}
