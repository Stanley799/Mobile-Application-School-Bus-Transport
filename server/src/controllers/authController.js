
// src/controllers/authController.js

/**
 * ===============================================
 *      School Bus Transport - Auth Controller
 * ===============================================
 * 
 * Handles the business logic for authentication.
 * - `login`: Validates user credentials, compares hashed passwords, and issues a JWT.
 * - `register`: Creates a new user, hashes their password, and saves to the DB.
 */

const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

/**
 * Handles user login.
 * @param {import('express').Request} req - The Express request object.
 * @param {import('express').Response} res - The Express response object.
 */
exports.login = async (req, res) => {
    const { email, password } = req.body;
    const prisma = req.prisma;

    // Input validation
    if (!email || !password) {
        return res.status(400).json({ error: 'Email and password are required' });
    }

    try {
        // Find the user by email
        const user = await prisma.users.findUnique({
            where: { email },
        });
        if (process.env.NODE_ENV === 'development') {
            console.log('User lookup by email:', Boolean(user));
        }
        let passwordMatch = false;
        if (user) {
            passwordMatch = await bcrypt.compare(password, user.password);
            if (process.env.NODE_ENV === 'development') {
                console.log('Password match:', passwordMatch);
            }
        }
        // If user not found or password doesn't match, return error
        if (!user || !passwordMatch) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        // Generate JWT
        const token = jwt.sign(
            { userId: user.id, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '7d' } // Token expires in 7 days
        );

        // Login successful, return token and user info
        res.status(200).json({
            message: 'Login successful',
            token,
            userId: user.id,
            role: user.role,
        });

    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Handles new user registration.
 * @param {import('express').Request} req - The Express request object.
 * @param {import('express').Response} res - The Express response object.
 */
exports.register = async (req, res) => {
    const { email, password, name, role, phone } = req.body;
    const prisma = req.prisma;

    // Basic validation
    if (!email || !password || !name || !role) {
        return res.status(400).json({ error: 'Email, password, name, and role are required' });
    }

    try {
        // Check if user already exists
    const existingUser = await prisma.users.findUnique({ where: { email } });
        if (existingUser) {
            return res.status(409).json({ error: 'User with this email already exists' });
        }

        // Hash the password before saving
        const saltRounds = 10;
        const hashedPassword = await bcrypt.hash(password, saltRounds);

        // Create the new user
    const newUser = await prisma.users.create({
            data: {
                email,
                password: hashedPassword,
                name,
                role, // Assuming role is one of 'ADMIN', 'DRIVER', 'PARENT'
                phone,
            },
        });

        res.status(201).json({
            message: 'User registered successfully',
            user: {
                id: newUser.id,
                email: newUser.email,
                name: newUser.name,
                role: newUser.role,
            },
        });

    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};
