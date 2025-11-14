
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

// Secure all location routes
router.use(authMiddleware);

// POST /api/locations - Update driver location
router.post('/', locationController.updateLocation);

// GET /api/locations/trip/:tripId - Get location history for a trip
router.get('/trip/:tripId', locationController.getTripLocations);

// GET /api/locations/trip/:tripId/latest - Get latest location for a trip
router.get('/trip/:tripId/latest', locationController.getLatestLocation);

module.exports = router;

