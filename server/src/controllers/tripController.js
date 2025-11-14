/**
 * Submits parent feedback for a trip.
 * Only parents whose child was on the trip can submit feedback.
 * Body: { rating: 1-5, comment: string, studentId?: number }
 */
exports.submitTripFeedback = async (req, res) => {
    const prisma = req.prisma;
    const user = req.user;
    const tripId = Number.parseInt(req.params.id, 10);
    const { rating, comment, studentId } = req.body;
    if (Number.isNaN(tripId)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }
    if (!rating || rating < 1 || rating > 5) {
        return res.status(400).json({ error: 'Rating must be 1-5' });
    }
    try {
        // Find parent record
        const parent = await prisma.parents.findUnique({ where: { user_id: user.userId } });
        if (!parent) {
            return res.status(403).json({ error: 'Parent record not found' });
        }
        // Check if parent's child was on the trip
        const trip = await prisma.trip.findUnique({
            where: { id: tripId },
            include: { trip_attendance_list: { include: { student: true } } }
        });
        if (!trip) {
            return res.status(404).json({ error: 'Trip not found' });
        }
        const studentIds = trip.trip_attendance_list.map(tal => tal.student.parent_id === parent.id ? tal.student.id : null).filter(Boolean);
        if (studentIds.length === 0) {
            return res.status(403).json({ error: 'You do not have a child on this trip' });
        }
        // If studentId is provided, check it's valid
        let feedbackStudentId = null;
        if (studentId) {
            if (!studentIds.includes(studentId)) {
                return res.status(403).json({ error: 'Student is not your child on this trip' });
            }
            feedbackStudentId = studentId;
        }
        // Prevent duplicate feedback per parent/trip/student
        const existing = await prisma.trip_feedback.findFirst({
            where: { trip_id: tripId, parent_id: parent.id, student_id: feedbackStudentId }
        });
        if (existing) {
            return res.status(409).json({ error: 'Feedback already submitted' });
        }
        const feedback = await prisma.trip_feedback.create({
            data: {
                trip_id: tripId,
                parent_id: parent.id,
                student_id: feedbackStudentId,
                rating,
                comment
            }
        });
        res.status(201).json(feedback);
    } catch (error) {
        console.error('Error submitting feedback:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets all feedback for a trip. Admins/Drivers see all, parents see only their own.
 */
exports.getTripFeedback = async (req, res) => {
    const prisma = req.prisma;
    const user = req.user;
    const tripId = Number.parseInt(req.params.id, 10);
    if (Number.isNaN(tripId)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }
    try {
        let where = { trip_id: tripId };
        if (user.role === 'PARENT') {
            const parent = await prisma.parents.findUnique({ where: { user_id: user.userId } });
            if (!parent) {
                return res.status(403).json({ error: 'Parent record not found' });
            }
            where.parent_id = parent.id;
        }
        const feedbacks = await prisma.trip_feedback.findMany({
            where,
            include: {
                parent: { select: { parent_fname: true, parent_lname: true } },
                student: { select: { student_fname: true, student_lname: true } }
            },
            orderBy: { created_at: 'desc' }
        });
        res.status(200).json(feedbacks);
    } catch (error) {
        console.error('Error fetching feedback:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

// src/controllers/tripController.js

/**
 * ===============================================
 *        School Bus Transport - Trip Controller
 * ===============================================
 * 
 * Handles all business logic for trip management.
 */

const pdfkit = require('pdfkit');

/**
 * Fetches all trips, with optional filters for role and date.
 * Role-based filtering:
 * - ADMIN: Can see all trips
 * - DRIVER: Can see only trips assigned to them
 * - PARENT: Can see only trips where their children are in the attendance list
 */
exports.getAllTrips = async (req, res) => {
    const { date, summary } = req.query;
    const prisma = req.prisma;
    const user = req.user;

    const where = {};

    // Role-based filtering
    if (user.role === 'DRIVER') {
        // Drivers see only their assigned trips
        where.driver = { user_id: user.userId };
    } else if (user.role === 'PARENT') {
        // Parents see only trips where their children are on the attendance list
        where.trip_attendance_list = {
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
        };
    }
    // ADMIN sees all trips (no filter)

    if (date) {
        const startDate = new Date(date);
        const endDate = new Date(date);
        endDate.setDate(endDate.getDate() + 1);
        where.trip_date = { gte: startDate, lt: endDate };
    }

    try {
        const isSummary = String(summary).toLowerCase() === 'true';
        const trips = await prisma.trip.findMany({
            where,
            include: {
                route: true,
                bus: true,
                driver: {
                    select: {
                        driver_fname: true,
                        driver_lname: true,
                        user: { select: { phone: true, name: true } },
                    },
                },
                ...(isSummary ? {} : {
                    trip_attendance_list: {
                        select: {
                            student: { 
                                select: { 
                                    id: true, 
                                    student_fname: true, 
                                    student_lname: true,
                                    grade: true,
                                    pickup_latitude: true,
                                    pickup_longitude: true
                                } 
                            },
                        },
                    }
                }),
            },
            orderBy: { trip_date: 'desc' },
        });
        res.status(200).json(trips);
    } catch (error) {
        console.error("Error fetching trips:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Creates a new trip. (Admin only)
 */
exports.createTrip = async (req, res) => {
    if (req.user.role !== 'ADMIN') {
        return res.status(403).json({ error: "Forbidden" });
    }

    const { busId, routeId, driverId, studentIds, tripDate, tripName, status } = req.body;
    const prisma = req.prisma;

    try {
        // Get bus, route, and driver info for trip_id generation
        const bus = await prisma.bus.findUnique({ where: { id: busId } });
        const route = await prisma.route.findUnique({ where: { id: routeId } });
        const driver = await prisma.drivers.findUnique({ 
            where: { id: driverId },
            include: { user: true }
        });

        if (!bus || !route || !driver) {
            return res.status(404).json({ error: 'Bus, route, or driver not found' });
        }

        // Generate unique trip_id
        const tripId = `TRIP-${Date.now()}-${bus.number_plate}`;

        const trip = await prisma.trip.create({
            data: {
                trip_id: tripId,
                bus: { connect: { id: busId } },
                route: { connect: { id: routeId } },
                driver: { connect: { id: driverId } },
                trip_date: tripDate ? new Date(tripDate) : new Date(),
                trip_name: tripName || `Trip-${Date.now()}`,
                status: status || 'SCHEDULED',
                bus_name: bus.bus_name || bus.number_plate,
                route_name: route.route_name,
                driver_name: `${driver.driver_fname} ${driver.driver_lname}`,
            },
        });

        // Optionally attach students into trip_attendance_list
        if (Array.isArray(studentIds) && studentIds.length > 0) {
            await prisma.trip_attendance_list.createMany({
                data: studentIds.map((sid) => ({ trip_id: trip.id, student_id: sid })),
                skipDuplicates: true,
            });
        }
        res.status(201).json(trip);
    } catch (error) {
        console.error("Error creating trip:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Fetches a single trip by its ID.
 * Role-based authorization:
 * - ADMIN: Can view any trip
 * - DRIVER: Can view only trips assigned to them
 * - PARENT: Can view only trips where their children are on the attendance list
 */
exports.getTripById = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const user = req.user;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        let trip;

        // Role-based authorization
        if (user.role === 'ADMIN') {
            trip = await prisma.trip.findUnique({
                where: { id: idInt },
                include: {
                    route: true,
                    bus: true,
                    driver: {
                        select: {
                            driver_fname: true,
                            driver_lname: true,
                            user: { select: { phone: true, name: true } },
                        },
                    },
                    trip_attendance_list: { 
                        include: { 
                            student: {
                                select: {
                                    id: true,
                                    student_fname: true,
                                    student_lname: true,
                                    grade: true,
                                    pickup_latitude: true,
                                    pickup_longitude: true
                                }
                            }
                        }
                    },
                    attendance: true,
                },
            });
        } else if (user.role === 'DRIVER') {
            trip = await prisma.trip.findFirst({
                where: {
                    id: idInt,
                    driver: {
                        user_id: user.userId
                    }
                },
                include: {
                    route: true,
                    bus: true,
                    driver: {
                        select: {
                            driver_fname: true,
                            driver_lname: true,
                            user: { select: { phone: true, name: true } },
                        },
                    },
                    trip_attendance_list: { 
                        include: { 
                            student: {
                                select: {
                                    id: true,
                                    student_fname: true,
                                    student_lname: true,
                                    grade: true,
                                    pickup_latitude: true,
                                    pickup_longitude: true
                                }
                            }
                        }
                    },
                    attendance: true,
                },
            });
        } else if (user.role === 'PARENT') {
            trip = await prisma.trip.findFirst({
                where: {
                    id: idInt,
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
                },
                include: {
                    route: true,
                    bus: true,
                    driver: {
                        select: {
                            driver_fname: true,
                            driver_lname: true,
                            user: { select: { phone: true, name: true } },
                        },
                    },
                    trip_attendance_list: { 
                        include: { 
                            student: {
                                select: {
                                    id: true,
                                    student_fname: true,
                                    student_lname: true,
                                    grade: true,
                                    pickup_latitude: true,
                                    pickup_longitude: true
                                },
                                where: {
                                    parent: {
                                        user_id: user.userId
                                    }
                                }
                            }
                        } 
                    },
                    attendance: {
                        where: {
                            student: {
                                parent: {
                                    user_id: user.userId
                                }
                            }
                        },
                        include: {
                            student: true
                        }
                    },
                },
            });
        }

        if (!trip) {
            return res.status(404).json({ error: 'Trip not found or you do not have access to this trip' });
        }

        res.status(200).json(trip);
    } catch (error) {
        console.error("Error fetching trip:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Starts a trip. (Driver only)
 */
exports.startTrip = async (req, res) => {
    if (req.user.role !== 'DRIVER') {
        return res.status(403).json({ error: "Forbidden" });
    }
    
    const { id } = req.params;
    const prisma = req.prisma;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        const updatedTrip = await prisma.trip.update({
            where: { id: idInt },
            data: { status: 'IN_PROGRESS', start: new Date().toISOString() },
        });
        
        // Notify clients via WebSocket
        req.io.to(`trip-${idInt}`).emit('trip-started', { tripId: idInt, status: 'IN_PROGRESS' });

        res.status(200).json({ message: "Trip started", trip: updatedTrip });
    } catch (error) {
        console.error("Error starting trip:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Ends a trip. (Driver only)
 */
exports.endTrip = async (req, res) => {
    if (req.user.role !== 'DRIVER') {
        return res.status(403).json({ error: "Forbidden" });
    }

    const { id } = req.params;
    const prisma = req.prisma;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        const updatedTrip = await prisma.trip.update({
            where: { id: idInt },
            data: { status: 'COMPLETED', stop: new Date().toISOString() },
        });

        req.io.to(`trip-${idInt}`).emit('trip-ended', { tripId: idInt, status: 'COMPLETED' });

        res.status(200).json({ message: "Trip ended", trip: updatedTrip });
    } catch (error) {
        console.error("Error ending trip:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Marks attendance for a student on a trip.
 */
exports.markAttendance = async (req, res) => {
    if (req.user.role !== 'DRIVER') {
        return res.status(403).json({ error: "Forbidden" });
    }

    const { id: tripId } = req.params;
    const { studentId, status } = req.body; // status: 'PRESENT' or 'ABSENT'
    const prisma = req.prisma;
    const tripIdInt = Number.parseInt(tripId, 10);
    const studentIdInt = Number.parseInt(studentId, 10);
    if (Number.isNaN(tripIdInt) || Number.isNaN(studentIdInt)) {
        return res.status(400).json({ error: 'Invalid trip or student id' });
    }

    try {
        const attendance = await prisma.attendance.upsert({
            where: { trip_id_student_id: { trip_id: tripIdInt, student_id: studentIdInt } },
            update: { status, timestamp: new Date(), marked_by: req.user.userId },
            create: { trip_id: tripIdInt, student_id: studentIdInt, status, timestamp: new Date(), marked_by: req.user.userId },
        });

        req.io.to(`trip-${tripIdInt}`).emit('attendance-updated', attendance);

        res.status(201).json(attendance);
    } catch (error) {
        console.error("Error marking attendance:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};

/**
 * Generates a PDF report for a trip.
 */
exports.getTripReport = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid trip id' });
    }

    try {
        const trip = await prisma.trip.findUnique({
            where: { id: idInt },
            include: { route: true, bus: true, driver: { include: { user: true } }, attendance: { include: { student: true } } },
        });

        if (!trip) {
            return res.status(404).json({ error: 'Trip not found' });
        }

        // Create a PDF document
        const doc = new pdfkit();
        let buffers = [];
        doc.on('data', buffers.push.bind(buffers));
        doc.on('end', () => {
            let pdfData = Buffer.concat(buffers);
            res.writeHead(200, {
                'Content-Length': Buffer.byteLength(pdfData),
                'Content-Type': 'application/pdf',
                'Content-disposition': `attachment;filename=trip-report-${trip.id}.pdf`,
            }).end(pdfData);
        });

        // Add content to the PDF
        doc.fontSize(20).text(`Trip Report: ${trip.route.route_name}`, { align: 'center' });
        doc.fontSize(12).moveDown();
        doc.text(`Date: ${new Date(trip.trip_date).toLocaleDateString()}`);
        doc.text(`Driver: ${trip.driver.driver_fname} ${trip.driver.driver_lname}`);
        doc.text(`Bus: ${trip.bus.number_plate}`);
        doc.moveDown();
        doc.fontSize(16).text('Attendance');
        doc.moveDown();

        trip.attendance.forEach(att => {
            const studentName = att.student ? `${att.student.student_fname} ${att.student.student_lname}` : att.student_id;
            doc.text(`${studentName}: ${att.status ?? ''} at ${new Date(att.timestamp).toLocaleTimeString()}`);
        });

        doc.end();

    } catch (error) {
        console.error("Error generating report:", error);
        res.status(500).json({ error: "Internal server error" });
    }
};
