
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

// Secure all trip routes
router.use(authMiddleware);

// GET /api/trips - Get all trips (with filters)
router.get('/', tripController.getAllTrips);

// POST /api/trips - Create a new trip (Admin only)
router.post('/', tripController.createTrip);

// GET /api/trips/:id - Get a single trip by ID
router.get('/:id', tripController.getTripById);

// PUT /api/trips/:id/start - Start a trip (Driver only)
router.put('/:id/start', tripController.startTrip);

// PUT /api/trips/:id/end - End a trip (Driver only)
router.put('/:id/end', tripController.endTrip);

// POST /api/trips/:id/attendance - Mark attendance for a student (Driver only)
router.post('/:id/attendance', tripController.markAttendance);

// GET /api/trips/:id/report - Get a trip report (Admin/Parent)
router.get('/:id/report', tripController.getTripReport);

module.exports = router;
