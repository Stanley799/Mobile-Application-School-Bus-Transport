
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

// Secure all message routes
router.use(authMiddleware);

// POST /api/messages - Send a message
router.post('/', messageController.sendMessage);

// GET /api/messages/conversations - Get all conversations
router.get('/conversations', messageController.getConversations);


// GET /api/messages/recipients - Get available recipients for messaging
router.get('/recipients', messageController.getAvailableRecipients);

// GET /api/messages/:userId - Get messages with a specific user
router.get('/:userId', messageController.getMessages);

module.exports = router;

