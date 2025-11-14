
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

// Middleware to protect routes
router.use(authMiddleware);

// Route to get user profile
// GET /api/users/profile/:id
router.get('/profile/:id', userController.getUserProfile);

// Route to get own profile (avoids id mismatch issues)
// GET /api/users/me
router.get('/me', userController.getMyProfile);

// Route to update user profile
// PUT /api/users/update/:id
router.put('/update/:id', userController.updateUserProfile);

module.exports = router;
