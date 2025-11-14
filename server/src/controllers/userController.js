
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
