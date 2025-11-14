// src/routes/admin.js

const express = require('express')
const router = express.Router()
const authMiddleware = require('../middleware/authMiddleware')
const adminController = require('../controllers/adminController')

// All endpoints require a valid JWT; controller ensures ADMIN role
router.use(authMiddleware)

// GET /api/admin/buses
router.get('/buses', adminController.listBuses)

// GET /api/admin/routes
router.get('/routes', adminController.listRoutes)

// GET /api/admin/drivers
router.get('/drivers', adminController.listDrivers)

module.exports = router
