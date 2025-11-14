const path = require('path');
const fs = require('fs');

/**
 * Upload or update user profile image.
 * POST /api/users/:id/profile-image
 * Expects multipart/form-data with 'image' field.
 */
exports.uploadProfileImage = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const requestingUser = req.user;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid user id' });
    }
    if (requestingUser.userId !== idInt) {
        return res.status(403).json({ error: 'Forbidden: You can only update your own profile image' });
    }
    if (!req.file) {
        return res.status(400).json({ error: 'No image file uploaded' });
    }
    // Save file path (relative)
    const imageUrl = `/uploads/profile/${req.file.filename}`;
    try {
        await prisma.users.update({ where: { id: idInt }, data: { image: imageUrl } });
        res.json({ message: 'Profile image uploaded', imageUrl });
    } catch (e) {
        console.error('Profile image upload error:', e);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Delete user profile image.
 * DELETE /api/users/:id/profile-image
 */
exports.deleteProfileImage = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const requestingUser = req.user;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid user id' });
    }
    if (requestingUser.userId !== idInt) {
        return res.status(403).json({ error: 'Forbidden: You can only delete your own profile image' });
    }
    try {
        const user = await prisma.users.findUnique({ where: { id: idInt } });
        if (user && user.image) {
            const filePath = path.join(__dirname, '../../', user.image);
            if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        }
        await prisma.users.update({ where: { id: idInt }, data: { image: null } });
        res.json({ message: 'Profile image deleted' });
    } catch (e) {
        console.error('Profile image delete error:', e);
        res.status(500).json({ error: 'Internal server error' });
    }
};

// src/controllers/userController.js

/**
 * ===============================================
 *       School Bus Transport - User Controller
 * ===============================================
 * 
 * Handles business logic for user-related actions.
 * - `getUserProfile`: Fetches a user's profile, excluding sensitive data.
 * - `updateUserProfile`: Updates a user's name and phone number.
 */

/**
 * Fetches a user's profile information.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 */
exports.getUserProfile = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const requestingUser = req.user; // from authMiddleware
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid user id' });
    }

    // Security check: Users can only fetch their own profile unless they are an admin
    if (requestingUser.role !== 'ADMIN' && requestingUser.userId !== idInt) {
        return res.status(403).json({ error: 'Forbidden: You can only view your own profile' });
    }

    try {
    const user = await prisma.users.findUnique({
            where: { id: idInt },
            select: { // Explicitly select fields to return, excluding password
                id: true,
                email: true,
                name: true,
                phone: true,
                role: true,
            },
        });

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.status(200).json(user);
    } catch (error) {
        console.error('Error fetching user profile:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Updates a user's profile.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 */
exports.updateUserProfile = async (req, res) => {
    const { id } = req.params;
    const { name, phone } = req.body;
    const prisma = req.prisma;
    const requestingUser = req.user;
    const idInt = Number.parseInt(id, 10);
    if (Number.isNaN(idInt)) {
        return res.status(400).json({ error: 'Invalid user id' });
    }

    // Users can only update their own profile
    if (requestingUser.userId !== idInt) {
        return res.status(403).json({ error: 'Forbidden: You can only update your own profile' });
    }

    try {
    const updatedUser = await prisma.users.update({
            where: { id: idInt },
            data: {
                name,
                phone,
            },
            select: {
                id: true,
                email: true,
                name: true,
                phone: true,
                role: true,
            },
        });

        res.status(200).json({
            message: 'Profile updated successfully',
            user: updatedUser,
        });

    } catch (error) {
        console.error('Error updating user profile:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Fetches the authenticated user's own profile without requiring an id param.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 */
exports.getMyProfile = async (req, res) => {
    const prisma = req.prisma;
    const requestingUser = req.user;

    try {
        const user = await prisma.users.findUnique({
            where: { id: requestingUser.userId },
            select: {
                id: true,
                email: true,
                name: true,
                phone: true,
                role: true,
            },
        });

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        res.status(200).json(user);
    } catch (error) {
        console.error('Error fetching my profile:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
