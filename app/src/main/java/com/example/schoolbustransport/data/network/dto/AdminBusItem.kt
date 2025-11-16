package com.example.schoolbustransport.data.network.dto

data class AdminBusItem(
    val id: String,
    val numberPlate: String?,
    val busName: String?,
    val capacity: Int?,
    val status: String?
)
