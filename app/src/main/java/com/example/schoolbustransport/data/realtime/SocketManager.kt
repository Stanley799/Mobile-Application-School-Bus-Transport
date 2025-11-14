package com.example.schoolbustransport.data.realtime

import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URISyntaxException

/**
 * SocketManager - Wrapper for Socket.IO client for real-time communication
 * 
 * Purpose:
 * - Establishes WebSocket connection for live bus tracking
 * - Authenticates using JWT token in handshake
 * - Joins trip-specific rooms for location broadcasts
 * - Emits driver location updates to server
 * 
 * Connection Flow:
 * 1. Normalizes API base URL to WebSocket base (removes /api/ suffix)
 * 2. Configures Socket.IO with reconnection settings
 * 3. Passes JWT token in auth payload for server validation
 * 4. Establishes connection and joins trip room
 * 
 * Events:
 * - "location-broadcast": Receives bus location updates from server
 * - "error": Receives error notifications
 * - "join-trip": Emits to join a specific trip room
 * - "location-update": Emits driver location to server
 * 
 * @param apiBaseUrl Base URL from BuildConfig (typically ends with /api/)
 * @param token JWT authentication token for WebSocket handshake
 */
class SocketManager(
	apiBaseUrl: String,
	token: String
) {
	private val socket: Socket

	init {
		// Normalize REST API base URL to WebSocket base URL
		// Example: "https://server.com/api/" -> "https://server.com"
		var base = apiBaseUrl
		if (base.endsWith("/")) base = base.dropLast(1)
		if (base.endsWith("/api")) base = base.dropLast(4)
		val httpUrl = base.toHttpUrlOrNull() 
			?: throw IllegalArgumentException("Invalid BASE_URL: $apiBaseUrl")

		// Configure Socket.IO connection options
		val options = IO.Options().apply {
			reconnection = true // Automatically reconnect on disconnect
			reconnectionAttempts = Int.MAX_VALUE // Retry indefinitely
			reconnectionDelay = 1000 // Initial delay: 1 second
			reconnectionDelayMax = 10_000 // Max delay: 10 seconds
			// Pass JWT token in auth payload for server-side validation
			// Server middleware will extract and validate this token
			auth = mapOf("token" to token)
		}

		try {
			socket = IO.socket(httpUrl.toString(), options)
		} catch (e: URISyntaxException) {
			throw IllegalArgumentException("Bad websocket URL: ${httpUrl}", e)
		}
	}

	/**
	 * Establishes WebSocket connection to server
	 * 
	 * Safe to call multiple times - only connects if not already connected.
	 * Connection will trigger handshake with JWT authentication.
	 */
	fun connect() {
		if (!socket.connected()) socket.connect()
	}

	/**
	 * Closes WebSocket connection
	 * 
	 * Safe to call multiple times - only disconnects if currently connected.
	 * Should be called when leaving a screen that uses real-time updates.
	 */
	fun disconnect() {
		if (socket.connected()) socket.disconnect()
	}

	/**
	 * Registers a listener for a specific Socket.IO event
	 * 
	 * @param event Event name to listen for (e.g., "location-broadcast", "error")
	 * @param listener Callback function that receives event arguments
	 */
	fun on(event: String, listener: (Array<out Any>) -> Unit) {
		socket.on(event) { args -> listener(args) }
	}

	/**
	 * Unregisters all listeners for a specific event
	 * 
	 * Should be called during cleanup to prevent memory leaks.
	 * 
	 * @param event Event name to stop listening for
	 */
	fun off(event: String) {
		socket.off(event)
	}

	/**
	 * Joins a trip room on the server
	 * 
	 * After joining, the client will receive location broadcasts
	 * for this specific trip. Server validates that the user has
	 * permission to view this trip based on their role.
	 * 
	 * @param tripId The ID of the trip to join
	 */
	fun joinTrip(tripId: Int) {
		socket.emit("join-trip", tripId)
	}

	/**
	 * Emits a driver location update to the server
	 * 
	 * Used by drivers to share their current location during active trips.
	 * Server validates that the user is the assigned driver for this trip
	 * and that the trip is IN_PROGRESS before broadcasting to other clients.
	 * 
	 * @param tripId The ID of the trip this location belongs to
	 * @param latitude Current latitude coordinate
	 * @param longitude Current longitude coordinate
	 * @param speed Optional current speed in m/s
	 * @param heading Optional current heading/bearing in degrees
	 */
	fun sendLocation(
		tripId: Int,
		latitude: Double,
		longitude: Double,
		speed: Float? = null,
		heading: Float? = null
	) {
		val payload = mutableMapOf<String, Any>(
			"tripId" to tripId,
			"latitude" to latitude,
			"longitude" to longitude
		)
		// Add optional fields if provided
		speed?.let { payload["speed"] = it }
		heading?.let { payload["heading"] = it }
		socket.emit("location-update", payload)
	}
}
