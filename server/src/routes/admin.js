// src/routes/admin.js

const express = require('express')
const router = express.Router()

const authMiddleware = require('../middleware/authMiddleware')
const roleMiddleware = require('../middleware/roleMiddleware')
const adminController = require('../controllers/adminController')

// All endpoints require a valid JWT
router.use(authMiddleware)

// GET /api/admin/buses (ADMIN only)
router.get('/buses', roleMiddleware(['ADMIN']), adminController.listBuses)

// GET /api/admin/routes (ADMIN only)
router.get('/routes', roleMiddleware(['ADMIN']), adminController.listRoutes)

// GET /api/admin/drivers (ADMIN only)
router.get('/drivers', roleMiddleware(['ADMIN']), adminController.listDrivers)

module.exports = router
