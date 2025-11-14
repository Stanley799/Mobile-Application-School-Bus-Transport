
// src/routes/messages.js

/**
 * ===============================================
 *    School Bus Transport - Message Routes
 * ===============================================
 * 
 * Defines endpoints for messaging.
 */

const express = require('express');
const router = express.Router();

const messageController = require('../controllers/messageController');
const authMiddleware = require('../middleware/authMiddleware');
const roleMiddleware = require('../middleware/roleMiddleware');

// Secure all message routes
router.use(authMiddleware);

// POST /api/messages - Send a message (ADMIN, DRIVER, PARENT)
router.post('/', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), messageController.sendMessage);

// GET /api/messages/conversations - Get all conversations (ADMIN, DRIVER, PARENT)
router.get('/conversations', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), messageController.getConversations);

// GET /api/messages/recipients - Get available recipients for messaging (ADMIN, DRIVER, PARENT)
router.get('/recipients', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), messageController.getAvailableRecipients);

// GET /api/messages/:userId - Get messages with a specific user (ADMIN, DRIVER, PARENT)
router.get('/:userId', roleMiddleware(['ADMIN', 'DRIVER', 'PARENT']), messageController.getMessages);

module.exports = router;

