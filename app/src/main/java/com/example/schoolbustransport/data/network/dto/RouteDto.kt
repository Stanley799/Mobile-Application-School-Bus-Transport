package com.example.schoolbustransport.data.network.dto

import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.Waypoint

/**
 * DTO for route data.
 */
data class RouteDto(
    val id: String,
    val name: String,
    val description: String?,
    val waypoints: String // Assuming waypoints are a JSON string
)

/**
 * Mapper function to convert RouteDto to Route domain model.
 */
fun RouteDto.toRoute(): Route {
    // A proper implementation would use a JSON parser like Gson or Moshi
    // For simplicity, we'll manually parse a simple format.
    val waypointList = mutableListOf<Waypoint>()
    return Route(
        id = id,
        name = name,
        description = description,
        waypoints = waypointList
    )
}
