
// src/routes/locations.js

/**
 * ===============================================
 *    School Bus Transport - Location Routes
 * ===============================================
 * 
 * Defines endpoints for location tracking.
 */

const express = require('express');
const router = express.Router();

const locationController = require('../controllers/locationController');
const authMiddleware = require('../middleware/authMiddleware');
const roleMiddleware = require('../middleware/roleMiddleware');

// Secure all location routes
router.use(authMiddleware);

// POST /api/locations - Update driver location (DRIVER only)
router.post('/', roleMiddleware(['DRIVER']), locationController.updateLocation);

// GET /api/locations/trip/:tripId - Get location history for a trip (ADMIN, DRIVER, PARENT)
router.get('/trip/:tripId', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), locationController.getTripLocations);

// GET /api/locations/trip/:tripId/latest - Get latest location for a trip (ADMIN, DRIVER, PARENT)
router.get('/trip/:tripId/latest', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), locationController.getLatestLocation);

module.exports = router;

