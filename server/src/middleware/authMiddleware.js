
// src/middleware/authMiddleware.js

/**
 * ===============================================
 *    School Bus Transport - Auth Middleware
 * ===============================================
 * 
 * Middleware to protect routes by verifying JSON Web Tokens (JWT).
 * It checks for the `Authorization` header, validates the token,
 * and attaches the decoded user payload (userId, role) to the request object.
 */

const jwt = require('jsonwebtoken');

/**
 * Express middleware to verify JWT.
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
const authMiddleware = (req, res, next) => {
    // Get token from the Authorization header
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Unauthorized: No token provided' });
    }

    // Extract token from "Bearer <token>"
    const token = authHeader.split(' ')[1];

    try {
        // Verify the token using the secret key
        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        // Attach the decoded payload to the request object
        req.user = { userId: decoded.userId, role: decoded.role };

        // Proceed to the next middleware or route handler
        next();
    } catch (error) {
        if (error instanceof jwt.TokenExpiredError) {
            return res.status(401).json({ error: 'Unauthorized: Token has expired' });
        }
        return res.status(401).json({ error: 'Unauthorized: Invalid token' });
    }
};

module.exports = authMiddleware;
