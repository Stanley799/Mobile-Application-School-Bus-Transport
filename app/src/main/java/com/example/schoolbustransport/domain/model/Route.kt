package com.example.schoolbustransport.domain.model

/**
 * Represents a bus route with waypoints.
 */
data class Route(
    val id: String,
    val name: String,
    val description: String?,
    val waypoints: List<Waypoint>
)

data class Waypoint(
    val lat: Double,
    val lng: Double,
    val name: String
)
