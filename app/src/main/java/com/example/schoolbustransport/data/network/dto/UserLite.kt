package com.example.schoolbustransport.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserLite(
    val id: Int,
    val name: String,
    val role: String
)
