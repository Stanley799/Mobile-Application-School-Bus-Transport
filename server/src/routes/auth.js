
// src/routes/auth.js

/**
 * ===============================================
 *        School Bus Transport - Auth Routes
 * ===============================================
 * 
 * Defines endpoints related to user authentication.
 * - `/login`: Authenticates a user and returns a JWT.
 * - `/register`: Creates a new user.
 */

const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Route to handle user login
// POST /api/auth/login
router.post('/login', authController.login);

// Route to handle user registration
// POST /api/auth/register
router.post('/register', authController.register);

module.exports = router;
