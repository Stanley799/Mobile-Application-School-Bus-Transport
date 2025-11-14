
// src/routes/users.js

/**
 * ===============================================
 *        School Bus Transport - User Routes
 * ===============================================
 * 
 * Defines endpoints for user-related actions.
 * - `/profile/:id`: Fetches a user's profile.
 * - `/update/:id`: Updates a user's profile information.
 */

const express = require('express');
const router = express.Router();

const userController = require('../controllers/userController');
const authMiddleware = require('../middleware/authMiddleware');
const roleMiddleware = require('../middleware/roleMiddleware');

// Middleware to protect routes
router.use(authMiddleware);

// Route to get user profile (ADMIN only)
router.get('/profile/:id', roleMiddleware(['ADMIN']), userController.getUserProfile);

// Route to get own profile (ADMIN, DRIVER, PARENT)
router.get('/me', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), userController.getMyProfile);

// Route to update user profile (ADMIN, DRIVER, PARENT)
router.put('/update/:id', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), userController.updateUserProfile);

module.exports = router;
