
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
const roleMiddleware = require('../middleware/roleMiddleware');

// Secure all student routes
router.use(authMiddleware);

// POST /api/students - Create a new student (ADMIN only)
router.post('/', roleMiddleware(['ADMIN']), studentController.createStudent);

// GET /api/students - Get all students (ADMIN, DRIVER, PARENT)
router.get('/', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), studentController.getStudents);

// GET /api/students/:id - Get a student by ID (ADMIN, DRIVER, PARENT)
router.get('/:id', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), studentController.getStudentById);

// DELETE /api/students/:id - Delete a student (ADMIN only)
router.delete('/:id', roleMiddleware(['ADMIN']), studentController.deleteStudent);

// PUT /api/students/:id - Update a student (ADMIN only)
router.put('/:id', roleMiddleware(['ADMIN']), studentController.updateStudent);

module.exports = router;

