
// src/routes/trips.js

/**
 * ===============================================
 *        School Bus Transport - Trip Routes
 * ===============================================
 * 
 * Defines endpoints for trip management.
 * - CRUD operations for trips.
 * - Starting and ending trips.
 * - Marking student attendance.
 * - Fetching reports.
 */

const express = require('express');
const router = express.Router();

const tripController = require('../controllers/tripController');
const authMiddleware = require('../middleware/authMiddleware');
const roleMiddleware = require('../middleware/roleMiddleware');

// Secure all trip routes
router.use(authMiddleware);

// GET /api/trips - Get all trips (ADMIN, DRIVER, PARENT)
router.get('/', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), tripController.getAllTrips);

// POST /api/trips - Create a new trip (ADMIN only)
router.post('/', roleMiddleware(['ADMIN']), tripController.createTrip);

// GET /api/trips/:id - Get a single trip by ID (ADMIN, DRIVER, PARENT)
router.get('/:id', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), tripController.getTripById);

// PUT /api/trips/:id/start - Start a trip (DRIVER only)
router.put('/:id/start', roleMiddleware(['DRIVER']), tripController.startTrip);

// PUT /api/trips/:id/end - End a trip (DRIVER only)
router.put('/:id/end', roleMiddleware(['DRIVER']), tripController.endTrip);

// POST /api/trips/:id/attendance - Mark attendance for a student (DRIVER only)
router.post('/:id/attendance', roleMiddleware(['DRIVER']), tripController.markAttendance);

// GET /api/trips/:id/report - Get a trip report (ADMIN, PARENT)
router.get('/:id/report', roleMiddleware(['ADMIN', 'PARENT']), tripController.getTripReport);


// POST /api/trips/:id/feedback - Submit parent feedback (PARENT only)
router.post('/:id/feedback', roleMiddleware(['PARENT']), tripController.submitTripFeedback);

// GET /api/trips/:id/feedback - Get feedback for a trip (ADMIN, DRIVER, PARENT)
router.get('/:id/feedback', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), tripController.getTripFeedback);

module.exports = router;
