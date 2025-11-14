
// src/sockets.js

/**
 * ===============================================
 *     School Bus Transport - WebSocket Handler
 * ===============================================
 * 
 * Manages all real-time communication via Socket.io.
 * - `join-trip`: Allows a client (driver, parent) to join a room for a specific trip.
 * - `location-update`: Receives location data from a driver and broadcasts it to the trip's room.
 * - `disconnect`: Handles client disconnection.
 */

/**
 * Initializes and attaches WebSocket event listeners.
 * @param {import('socket.io').Server} io - The Socket.io server instance.
 * @param {import('@prisma/client').PrismaClient} prisma - The Prisma client instance.
 */
const jwt = require('jsonwebtoken');

function socketHandler(io, prisma) {
    // Socket.io middleware for authentication
    io.use(async (socket, next) => {
        try {
            const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.split(' ')[1];
            
            if (!token) {
                return next(new Error('Authentication error: No token provided'));
            }

            const decoded = jwt.verify(token, process.env.JWT_SECRET);
            socket.userId = decoded.userId;
            socket.userRole = decoded.role;
            next();
        } catch (error) {
            next(new Error('Authentication error: Invalid token'));
        }
    });

    // Event listener for a new client connection
    io.on('connection', async (socket) => {
        console.log(`🔌 A user connected: ${socket.id} (UserId: ${socket.userId}, Role: ${socket.userRole})`);
        // Join a personal room for direct messaging
        socket.join(`user-${socket.userId}`);

        // Listener for when a client wants to join a trip-specific room
        socket.on('join-trip', async (tripId) => {
            try {
                const tripIdInt = parseInt(tripId);
                if (isNaN(tripIdInt)) {
                    socket.emit('error', { message: 'Invalid trip id' });
                    return;
                }

                // Authorization check
                let authorized = false;

                if (socket.userRole === 'ADMIN') {
                    // Admins can join any trip
                    authorized = true;
                } else if (socket.userRole === 'DRIVER') {
                    // Drivers can join their assigned trips
                    const trip = await prisma.trip.findFirst({
                        where: {
                            id: tripIdInt,
                            driver: {
                                user_id: socket.userId
                            }
                        }
                    });
                    authorized = !!trip;
                } else if (socket.userRole === 'PARENT') {
                    // Parents can join trips where their children are on the attendance list
                    const trip = await prisma.trip.findFirst({
                        where: {
                            id: tripIdInt,
                            trip_attendance_list: {
                                some: {
                                    student: {
                                        parent_id: {
                                            not: null
                                        },
                                        parent: {
                                            user_id: socket.userId
                                        }
                                    }
                                }
                            }
                        }
                    });
                    authorized = !!trip;
                }

                if (!authorized) {
                    socket.emit('error', { message: 'Forbidden: You do not have access to this trip' });
                    return;
                }

                console.log(`[Socket ${socket.id}] joining trip room: trip-${tripId}`);
                socket.join(`trip-${tripId}`);
                socket.emit('joined-trip', { tripId, message: `You have joined the room for trip ${tripId}` });
            } catch (error) {
                console.error('Error joining trip:', error);
                socket.emit('error', { message: 'Error joining trip' });
            }
        });

        // Listener for location updates from a driver
        socket.on('location-update', async (data) => {
            try {
                const { tripId, latitude, longitude, speed, heading } = data || {};
                
                if (tripId === undefined || latitude === undefined || longitude === undefined) {
                    console.error('Invalid location update received:', data);
                    socket.emit('error', { message: 'Invalid location data' });
                    return;
                }

                // Only drivers can send location updates
                if (socket.userRole !== 'DRIVER') {
                    socket.emit('error', { message: 'Forbidden: Only drivers can send location updates' });
                    return;
                }

                const tripIdInt = parseInt(tripId);
                if (isNaN(tripIdInt)) {
                    socket.emit('error', { message: 'Invalid trip id' });
                    return;
                }

                // Verify driver is assigned to this trip
                const trip = await prisma.trip.findFirst({
                    where: {
                        id: tripIdInt,
                        driver: {
                            user_id: socket.userId
                        },
                        status: 'IN_PROGRESS'
                    },
                    include: {
                        driver: true
                    }
                });

                if (!trip) {
                    socket.emit('error', { message: 'Forbidden: You are not assigned to this trip or trip is not in progress' });
                    return;
                }

                // Store location in database
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

                console.log(`📍 Location update for trip ${tripId}: ${latitude}, ${longitude}`);

                // Broadcast the location to all clients in the same trip room (including sender)
                io.to(`trip-${tripId}`).emit('location-broadcast', {
                    tripId: trip.id,
                    latitude: location.latitude,
                    longitude: location.longitude,
                    speed: location.speed,
                    heading: location.heading,
                    timestamp: location.captured_at
                });
            } catch (error) {
                console.error('Error handling location update:', error);
                socket.emit('error', { message: 'Error updating location' });
            }
        });

        // Listener for when a client disconnects
        socket.on('disconnect', () => {
            console.log(`👋 A user disconnected: ${socket.id}`);
            // Attach io instance to req for controllers to use
            io.use((socket, next) => {
                socket.request.io = io;
                next();
            });
            // Attach io to req in REST middleware (Express)
            if (io.httpServer && io.httpServer.listeners) {
                const expressApp = io.httpServer.listeners('request')[0];
                if (expressApp) {
                    expressApp.use((req, res, next) => {
                        req.io = io;
                        next();
                    });
                }
            }
        });
    });
}

module.exports = socketHandler;
