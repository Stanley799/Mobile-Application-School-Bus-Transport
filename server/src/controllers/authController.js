const crypto = require('crypto');

// In-memory store for reset tokens (for demo; use DB/Redis in production)
const passwordResetTokens = {};

/**
 * Request password reset: send reset link to email if user exists
 * POST /api/auth/request-password-reset { email }
 */
exports.requestPasswordReset = async (req, res) => {
    const { email } = req.body;
    const prisma = req.prisma;
    if (!email) return res.status(400).json({ error: 'Email is required' });
    try {
        const user = await prisma.users.findUnique({ where: { email } });
        if (!user) return res.status(404).json({ error: 'No user with that email' });
        // Generate token
        const token = crypto.randomBytes(32).toString('hex');
        passwordResetTokens[token] = { userId: user.id, expires: Date.now() + 1000 * 60 * 30 };
        // Send email
        const resetUrl = `${process.env.FRONTEND_URL || 'http://localhost:3000'}/reset-password?token=${token}`;
        await sendMail(email, 'Password Reset', `<p>Click <a href="${resetUrl}">here</a> to reset your password. This link expires in 30 minutes.</p>`);
        res.json({ message: 'Password reset link sent to email' });
    } catch (e) {
        console.error('Password reset request error:', e);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Reset password using token
 * POST /api/auth/reset-password { token, newPassword }
 */
exports.resetPassword = async (req, res) => {
    const { token, newPassword } = req.body;
    const prisma = req.prisma;
    if (!token || !newPassword) return res.status(400).json({ error: 'Token and new password required' });
    // Validate strong password
    const strongPassword = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!strongPassword.test(newPassword)) {
        return res.status(400).json({ error: 'Password must be at least 8 characters and include upper, lower, number, and special character.' });
    }
    const entry = passwordResetTokens[token];
    if (!entry || entry.expires < Date.now()) {
        return res.status(400).json({ error: 'Invalid or expired token' });
    }
    try {
        const hashed = await bcrypt.hash(newPassword, 10);
        await prisma.users.update({ where: { id: entry.userId }, data: { password: hashed } });
        delete passwordResetTokens[token];
        res.json({ message: 'Password reset successful' });
    } catch (e) {
        console.error('Password reset error:', e);
        res.status(500).json({ error: 'Internal server error' });
    }
};

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

const { sendMail } = require('../utils/mailer');

exports.register = async (req, res) => {
    const { email, password, name, role, phone } = req.body;
    const prisma = req.prisma;

    // --- Input validation ---
    if (!email || !password || !name || !role || !phone) {
        return res.status(400).json({ error: 'Email, password, name, role, and phone are required' });
    }
    // Validate email format
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    if (!emailRegex.test(email)) {
        return res.status(400).json({ error: 'Invalid email format' });
    }
    // Validate phone format (simple, can be improved)
    const phoneRegex = /^[0-9\-\+]{9,15}$/;
    if (!phoneRegex.test(phone)) {
        return res.status(400).json({ error: 'Invalid phone number format' });
    }
    // Validate strong password (min 8 chars, upper, lower, number, special)
    const strongPassword = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!strongPassword.test(password)) {
        return res.status(400).json({ error: 'Password must be at least 8 characters and include upper, lower, number, and special character.' });
    }
    // Validate role
    const allowedRoles = ['ADMIN', 'DRIVER', 'PARENT'];
    if (!allowedRoles.includes(role)) {
        return res.status(400).json({ error: 'Invalid role selected' });
    }

    try {
        // Check if user already exists (email or phone)
        const existingUser = await prisma.users.findFirst({ where: { OR: [{ email }, { phone }] } });
        if (existingUser) {
            return res.status(409).json({ error: 'User with this email or phone already exists' });
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
                role,
                phone,
            },
        });

        // Send welcome/verification email (no verification token for now)
        try {
            await sendMail(
                email,
                'Welcome to School Bus Transport',
                `<h2>Welcome, ${name}!</h2><p>Your account has been created successfully. You can now log in and use the app.</p>`
            );
        } catch (mailErr) {
            // Log but do not fail registration
            console.error('Failed to send welcome email:', mailErr);
        }

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
