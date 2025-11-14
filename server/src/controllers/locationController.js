
// src/controllers/locationController.js

/**
 * ===============================================
 *   School Bus Transport - Location Controller
 * ===============================================
 * 
 * Handles location tracking for trips.
 * - `updateLocation`: Driver posts location updates
 * - `getTripLocations`: Get location history for a trip
 * - `getLatestLocation`: Get the latest location for a trip
 */

/**
 * Updates the driver's location for an active trip.
 * Only drivers assigned to the trip can update location.
 */
exports.updateLocation = async (req, res) => {
    const { tripId, latitude, longitude, speed, heading } = req.body;
    const prisma = req.prisma;
    const user = req.user;

    // Only drivers can update location
    if (user.role !== 'DRIVER') {
        return res.status(403).json({ error: 'Forbidden: Only drivers can update location' });
    }

    if (!tripId || latitude === undefined || longitude === undefined) {
        return res.status(400).json({ error: 'tripId, latitude, and longitude are required' });
    }

    try {
        // Verify the driver is assigned to this trip
        const trip = await prisma.trip.findFirst({
            where: {
                id: parseInt(tripId),
                driver: {
                    user_id: user.userId
                },
                status: 'IN_PROGRESS'
            },
            include: {
                driver: true
            }
        });

        if (!trip) {
            return res.status(403).json({ error: 'Forbidden: You are not assigned to this trip or trip is not in progress' });
        }

        // Store location
        const location = await prisma.trip_locations.create({
            data: {
                trip_id: trip.id,
                driver_id: trip.driver_id,
                latitude: parseFloat(latitude),
                longitude: parseFloat(longitude),
                speed: speed ? parseFloat(speed) : null,
                heading: heading ? parseFloat(heading) : null,
            }
        });

        // Broadcast via WebSocket
        req.io.to(`trip-${trip.id}`).emit('location-broadcast', {
            tripId: trip.id,
            latitude: location.latitude,
            longitude: location.longitude,
            speed: location.speed,
            heading: location.heading,
            timestamp: location.captured_at
        });

        res.status(201).json({
            message: 'Location updated',
            location: {
                id: location.id,
                latitude: location.latitude,
                longitude: location.longitude,
                timestamp: location.captured_at
            }
        });
    } catch (error) {
        console.error('Error updating location:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets location history for a trip.
 * Admins can view any trip, parents can view trips with their children, drivers can view their own trips.
 */
exports.getTripLocations = async (req, res) => {
    const { tripId } = req.params;
    const prisma = req.prisma;
    const user = req.user;
    const tripIdInt = parseInt(tripId);

    if (isNaN(tripIdInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        // Check authorization
        let trip;
        if (user.role === 'ADMIN') {
            trip = await prisma.trip.findUnique({
                where: { id: tripIdInt }
            });
        } else if (user.role === 'DRIVER') {
            trip = await prisma.trip.findFirst({
                where: {
                    id: tripIdInt,
                    driver: {
                        user_id: user.userId
                    }
                }
            });
        } else if (user.role === 'PARENT') {
            // Check if parent has a child on this trip
            trip = await prisma.trip.findFirst({
                where: {
                    id: tripIdInt,
                    trip_attendance_list: {
                        some: {
                            student: {
                                parent_id: {
                                    not: null
                                },
                                parent: {
                                    user_id: user.userId
                                }
                            }
                        }
                    }
                }
            });
        }

        if (!trip) {
            return res.status(403).json({ error: 'Forbidden: You do not have access to this trip' });
        }

        // Get locations (limit to last 100 for performance)
        const locations = await prisma.trip_locations.findMany({
            where: {
                trip_id: tripIdInt
            },
            orderBy: {
                captured_at: 'desc'
            },
            take: 100
        });

        res.status(200).json(locations);
    } catch (error) {
        console.error('Error fetching trip locations:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets the latest location for a trip.
 */
exports.getLatestLocation = async (req, res) => {
    const { tripId } = req.params;
    const prisma = req.prisma;
    const user = req.user;
    const tripIdInt = parseInt(tripId);

    if (isNaN(tripIdInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        // Check authorization (same logic as getTripLocations)
        let trip;
        if (user.role === 'ADMIN') {
            trip = await prisma.trip.findUnique({
                where: { id: tripIdInt }
            });
        } else if (user.role === 'DRIVER') {
            trip = await prisma.trip.findFirst({
                where: {
                    id: tripIdInt,
                    driver: {
                        user_id: user.userId
                    }
                }
            });
        } else if (user.role === 'PARENT') {
            trip = await prisma.trip.findFirst({
                where: {
                    id: tripIdInt,
                    trip_attendance_list: {
                        some: {
                            student: {
                                parent_id: {
                                    not: null
                                },
                                parent: {
                                    user_id: user.userId
                                }
                            }
                        }
                    }
                }
            });
        }

        if (!trip) {
            return res.status(403).json({ error: 'Forbidden: You do not have access to this trip' });
        }

        // Get latest location
        const location = await prisma.trip_locations.findFirst({
            where: {
                trip_id: tripIdInt
            },
            orderBy: {
                captured_at: 'desc'
            }
        });

        if (!location) {
            return res.status(404).json({ error: 'No location data found for this trip' });
        }

        res.status(200).json(location);
    } catch (error) {
        console.error('Error fetching latest location:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

