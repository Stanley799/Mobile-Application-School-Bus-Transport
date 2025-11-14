
// src/index.js

/**
 * ===============================================
 *      School Bus Transport System - Backend
 * ===============================================
 * 
 * Main entry point for the Node.js server.
 * 
 * Key functionalities:
 * - Initializes Express server.
 * - Integrates essential middleware (CORS, Helmet, JSON parser).
 * - Sets up Socket.io for real-time WebSocket communication.
 * - Defines API routes for different modules (auth, users, trips).
 * - Connects to the Prisma client for database operations.
 * - Loads environment variables using dotenv.
 */

// ----------------------------------------
//          Module Imports
// ----------------------------------------
require('dotenv').config(); // Load environment variables from .env file
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const { PrismaClient } = require('@prisma/client');

const authRoutes = require('./routes/auth');
const tripRoutes = require('./routes/trips');
const userRoutes = require('./routes/users');
const locationRoutes = require('./routes/locations');
const messageRoutes = require('./routes/messages');
const studentRoutes = require('./routes/students');
const adminRoutes = require('./routes/admin');
const socketHandler = require('./sockets');

// ----------------------------------------
//          Initialization
// ----------------------------------------
const app = express();
const server = http.createServer(app);
const prisma = new PrismaClient();

// Initialize Socket.io with CORS configuration
const io = new Server(server, {
    cors: {
        origin: process.env.CORS_ORIGIN || "*",
        methods: ["GET", "POST"],
    }
});

// ----------------------------------------
//          Middleware Setup
// ----------------------------------------
// Enable Cross-Origin Resource Sharing for all routes
app.use(cors({ origin: process.env.CORS_ORIGIN || "*" }));

// Secure HTTP headers
app.use(helmet());

// Parse incoming JSON requests
app.use(express.json());

// Middleware to make prisma and io accessible in all routes
// This is a simple dependency injection pattern for Express
app.use((req, res, next) => {
    req.prisma = prisma;
    req.io = io;
    next(); // Pass control to the next handler
});

// ----------------------------------------
//              API Routes
// ----------------------------------------
app.get('/', (req, res) => {
    res.json({ message: 'Welcome to the School Bus Transport API!', status: 'running' });
});

// Basic health endpoint for uptime checks
app.get('/healthz', (req, res) => {
    res.status(200).json({ status: 'ok' });
});

app.use('/api/auth', authRoutes);
app.use('/api/trips', tripRoutes);
app.use('/api/users', userRoutes);
app.use('/api/locations', locationRoutes);
app.use('/api/messages', messageRoutes);
app.use('/api/students', studentRoutes);
app.use('/api/admin', adminRoutes);

// ----------------------------------------
//        WebSocket Connection Handler
// ----------------------------------------
socketHandler(io, prisma);

// ----------------------------------------
//           Server Startup
// ----------------------------------------
const PORT = process.env.PORT || 3000;

server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server is running on http://localhost:${PORT}`);
    console.log('🔌 WebSocket server is listening for connections.');
});

// Centralized error handler to standardize error responses
// Must be after routes and before shutdown hooks
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    const status = err.status || 500;
    res.status(status).json({ errorCode: status, message: err.message || 'Internal server error' });
});

// ----------------------------------------
//           Graceful Shutdown
// ----------------------------------------
process.on('SIGINT', async () => {
    console.log('\n🚦 Received SIGINT. Shutting down gracefully...');
    await prisma.$disconnect();
    server.close(() => {
        console.log('✅ Server has been shut down.');
        process.exit(0);
    });
});
