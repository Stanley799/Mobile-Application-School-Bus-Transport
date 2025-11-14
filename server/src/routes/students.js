
// src/routes/students.js

/**
 * ===============================================
 *    School Bus Transport - Student Routes
 * ===============================================
 * 
 * Defines endpoints for student management.
 */

const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');
const authMiddleware = require('../middleware/authMiddleware');

// Secure all student routes
router.use(authMiddleware);

// POST /api/students - Create a new student
router.post('/', studentController.createStudent);

// GET /api/students - Get all students (role-based)
router.get('/', studentController.getStudents);

// GET /api/students/:id - Get a student by ID
router.get('/:id', studentController.getStudentById);

// PUT /api/students/:id - Update a student
router.put('/:id', studentController.updateStudent);

module.exports = router;

