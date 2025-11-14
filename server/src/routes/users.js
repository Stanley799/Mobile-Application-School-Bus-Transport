
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
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const userController = require('../controllers/userController');
const authMiddleware = require('../middleware/authMiddleware');
const roleMiddleware = require('../middleware/roleMiddleware');

// Multer config for profile images
const uploadDir = path.join(__dirname, '../../uploads/profile');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });
const storage = multer.diskStorage({
	destination: function (req, file, cb) { cb(null, uploadDir); },
	filename: function (req, file, cb) {
		const ext = path.extname(file.originalname);
		cb(null, `user_${req.params.id}_${Date.now()}${ext}`);
	}
});
const upload = multer({ storage });

// Middleware to protect routes
router.use(authMiddleware);

// Route to get user profile (ADMIN only)
router.get('/profile/:id', roleMiddleware(['ADMIN']), userController.getUserProfile);

// Route to get own profile (ADMIN, DRIVER, PARENT)
router.get('/me', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), userController.getMyProfile);

// Route to update user profile (ADMIN, DRIVER, PARENT)
router.put('/update/:id', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), userController.updateUserProfile);


// Profile image upload (authenticated user only)
router.post('/:id/profile-image', upload.single('image'), userController.uploadProfileImage);
// Profile image delete (authenticated user only)
router.delete('/:id/profile-image', userController.deleteProfileImage);

module.exports = router;
