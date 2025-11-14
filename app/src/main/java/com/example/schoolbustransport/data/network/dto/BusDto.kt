package com.example.schoolbustransport.data.network.dto

import com.example.schoolbustransport.domain.model.Bus

/**
 * DTO for bus data.
 */
data class BusDto(
    val id: String,
    val licensePlate: String,
    val model: String?,
    val capacity: Int
)

/**
 * Mapper function to convert BusDto to Bus domain model.
 */
fun BusDto.toBus(): Bus {
    return Bus(
        id = id,
        licensePlate = licensePlate,
        model = model,
        capacity = capacity
    )
}
