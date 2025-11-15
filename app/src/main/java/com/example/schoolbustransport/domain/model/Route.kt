package com.example.schoolbustransport.domain.model

/**
 * Represents a bus route with waypoints.
 */
data class Route(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val waypoints: List<Waypoint> = emptyList()
)

data class Waypoint(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val name: String = ""
)
