// src/middleware/roleMiddleware.js
/**
 * ===============================================
 *    School Bus Transport - Role Middleware
 * ===============================================
 *
 * Middleware to enforce role-based access control (RBAC) for Express routes.
 * Usage: router.get('/admin', roleMiddleware(['ADMIN']), handler)
 */

/**
 * Returns an Express middleware that checks if the authenticated user has one of the allowed roles.
 * @param {string[]} allowedRoles - Array of allowed roles (e.g., ['ADMIN', 'DRIVER'])
 * @returns {import('express').RequestHandler}
 */
function roleMiddleware(allowedRoles) {
    return (req, res, next) => {
        if (!req.user || !allowedRoles.includes(req.user.role)) {
            return res.status(403).json({ error: 'Forbidden: Insufficient permissions' });
        }
        next();
    };
}

module.exports = roleMiddleware;
